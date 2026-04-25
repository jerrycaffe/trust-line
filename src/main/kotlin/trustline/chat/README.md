# Trustline Chat System

A real-time messaging system that connects users with counsellors. Users can send messages without targeting a specific counsellor — any available counsellor can pick up and respond to the conversation.

---

## Table of Contents

- [Architecture](#architecture)
- [Room Lifecycle](#room-lifecycle)
- [Message Status (Delivered / Read)](#message-status-delivered--read)
- [Typing Indicators](#typing-indicators)
- [Online / Offline Presence](#online--offline-presence)
- [REST API](#rest-api)
- [WebSocket API](#websocket-api)
- [WebSocket Subscriptions Reference](#websocket-subscriptions-reference)
- [Authentication](#authentication)
- [Testing with Postman](#testing-with-postman)
- [Testing WebSocket in Browser](#testing-websocket-in-browser)

---

## Architecture

The chat system has two transport layers that share the same database:

| Transport | When to Use |
|---|---|
| **REST (HTTP)** | Sending messages, fetching history, claiming rooms, marking as read |
| **WebSocket (STOMP)** | Real-time message delivery to connected clients |

**Key components:**

```
ChatController           → REST endpoints
ChatWebSocketController  → STOMP handlers (/app/chat.send, /app/chat.delivered, /app/chat.typing)
ChatServiceImpl          → Business logic (shared by both transports)
PresenceService          → In-memory online/offline tracking (userId → active session IDs)
WebSocketEventListener   → Reacts to STOMP CONNECT/DISCONNECT to update presence
WebSocketConfig          → STOMP broker config (SockJS at /ws)
WebSocketAuthInterceptor → JWT validation on STOMP CONNECT
```

---

## Room Lifecycle

```
User sends first message
        │
        ▼
 ┌─────────────┐
 │  OPEN room  │  ← no counsellor assigned yet
 │             │     message broadcast to /topic/open-rooms
 └──────┬──────┘
        │  Counsellor claims room (POST /rooms/{id}/claim)
        │  OR counsellor sends first reply (chatRoomId in body)
        ▼
 ┌─────────────┐
 │ ACTIVE room │  ← counsellor assigned; messages go directly between the two
 └──────┬──────┘
        │  Future: close room
        ▼
 ┌─────────────┐
 │ CLOSED room │
 └─────────────┘
```

A user can only have **one OPEN room at a time**. Subsequent messages from the same user (without a `chatRoomId`) reuse the existing open room.

---

## Message Status (Delivered / Read)

Every message has a `messageStatus` field in its response payload. The value progresses through three states:

| Status | Meaning |
|---|---|
| `SENT` | Message was saved to the database. The recipient was **offline** at the time of sending. |
| `DELIVERED` | The recipient's device has received the message. Set automatically if the recipient was **online** when the message was sent, or explicitly when the client sends a delivery ACK. |
| `READ` | The recipient has opened the conversation and called `PUT /rooms/{id}/read`. |

### How status flows back to the sender

Whenever a message transitions to `DELIVERED` or `READ`, the **original sender** receives a real-time push on:

```
/user/{senderId}/queue/status
```

Payload (`MessageStatusUpdate`):

```json
{
  "messageId": "uuid",
  "chatRoomId": "uuid",
  "status": "DELIVERED"
}
```

### Sending a delivery ACK (client → server)

When the recipient's client receives a message (e.g. on the `/user/{id}/queue/messages` subscription), it should immediately acknowledge delivery:

```
SEND
destination:/app/chat.delivered
content-type:application/json

{
  "messageId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

This transitions the message to `DELIVERED` and notifies the sender.

### Marking messages as read (REST)

```
PUT /api/v1/chat/rooms/{chatRoomId}/read
Authorization: Bearer <token>
```

Marks all unread messages from the other participant as `READ` and notifies the sender.

---

## Typing Indicators

### Sending a typing event (client → server)

To inform the other participant that the current user is typing (or stopped typing):

```
SEND
destination:/app/chat.typing
content-type:application/json

{
  "chatRoomId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "typing": true
}
```

Set `"typing": false` when the user stops typing.

### Receiving typing events (server → client)

The other participant in the room receives a `TypingNotification` on:

```
/user/{recipientId}/queue/typing
```

Payload:

```json
{
  "chatRoomId": "uuid",
  "senderId": "uuid",
  "typing": true
}
```

Use this to show/hide a "... is typing" indicator in the UI.

---

## Online / Offline Presence

Presence is **automatic** — no API call is needed. The system tracks every WebSocket session by user ID (multi-device aware).

| Event | Trigger |
|---|---|
| User goes **online** | STOMP `CONNECT` frame received |
| User goes **offline** | STOMP `DISCONNECT` / connection drop (last session closed) |

### Receiving presence events (server → client)

Each participant in any shared chat room receives a `PresenceEvent` on:

```
/user/{userId}/queue/presence
```

Payload:

```json
{
  "userId": "uuid",
  "online": true
}
```

`online: false` is only emitted when the user's **last** active session disconnects (multi-tab safe).

---

## REST API

All endpoints require `Authorization: Bearer <jwt>`.

### Send a Message

```
POST /api/v1/chat/send
```

**Request body:**

| Field | Type | Required | Description |
|---|---|---|---|
| `content` | string | Yes | Message text |
| `chatRoomId` | UUID | No | Reply in a specific room (counsellor or user follow-up) |
| `recipientId` | UUID | No | Open a direct room with a known user (legacy) |

**Resolution logic:**
- `chatRoomId` provided → send in that room
- `recipientId` provided → find or create a direct room between the two users
- Neither provided → find or create an **OPEN** room for the sender (user-to-pool flow)

---

### List My Chat Rooms

```
GET /api/v1/chat/rooms
```

Returns all rooms where the caller is a participant. Each room includes the other participant's profile, the last message, unread count, and room status.

---

### List Open Rooms _(Counsellor only)_

```
GET /api/v1/chat/rooms/open
```

Returns all `OPEN` rooms waiting for a counsellor, ordered by creation time (oldest first).

---

### Get Messages in a Room

```
GET /api/v1/chat/rooms/{chatRoomId}/messages
```

Returns messages in ascending chronological order. Only participants of the room can access it.

---

### Mark Messages as Read

```
PUT /api/v1/chat/rooms/{chatRoomId}/read
```

Marks all messages sent by the other participant as read in the given room.

---

### Claim a Room _(Counsellor only)_

```
POST /api/v1/chat/rooms/{chatRoomId}/claim
```

Assigns the calling counsellor to an `OPEN` room and transitions it to `ACTIVE`. The user receives a real-time `COUNSELLOR_JOINED` WebSocket event. Returns the updated `ChatRoomDto`.

---

## WebSocket API

The server exposes a STOMP endpoint at `/ws` (wrapped in SockJS).

### Broker Destinations

| Destination | Direction | Description |
|---|---|---|
| `/app/chat.send` | Client → Server | Send a message |
| `/app/chat.delivered` | Client → Server | Acknowledge message delivery |
| `/app/chat.typing` | Client → Server | Send typing start/stop event |
| `/user/{id}/queue/messages` | Server → Client | Personal message delivery (and sender echo) |
| `/user/{id}/queue/status` | Server → Client | Message status updates (`DELIVERED` / `READ`) pushed to the original sender |
| `/user/{id}/queue/typing` | Server → Client | Typing notification from the other participant |
| `/user/{id}/queue/presence` | Server → Client | Online / offline status of contacts |
| `/topic/open-rooms` | Server → Client | Broadcast when a new message lands in an OPEN room |

### STOMP CONNECT

Include the JWT in the connection headers:

```
CONNECT
Authorization: Bearer <token>
```

The server reads this header, validates the JWT, and associates the connection with the user's ID as the STOMP principal.

### Send Message Frame

```
SEND
destination:/app/chat.send
content-type:application/json

{
  "content": "Hello"
}
```

To reply in a specific room, include `chatRoomId`:

```json
{
  "chatRoomId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "content": "Hello, how can I help?"
}
```

### Received Message Payload

```json
{
  "id": "uuid",
  "senderId": "uuid",
  "senderEmail": "user@example.com",
  "content": "Hello",
  "isRead": false,
  "messageStatus": "SENT",
  "createdAt": "2026-04-25T10:00:00"
}
```

`messageStatus` is one of `SENT`, `DELIVERED`, or `READ`. See [Message Status](#message-status-delivered--read) for the full flow.

### COUNSELLOR_JOINED Event

When a counsellor claims a room, the user receives this on `/user/{id}/queue/messages`:

```json
{
  "type": "COUNSELLOR_JOINED",
  "chatRoomId": "uuid"
}
```

---

## WebSocket Subscriptions Reference

Subscribe to these destinations immediately after the STOMP `CONNECT` succeeds. Replace `{id}` with the authenticated user's ID (UUID).

| Subscription destination | Payload type | When received |
|---|---|---|
| `/user/queue/messages` | `ChatMessageDto` or `COUNSELLOR_JOINED` event | A new message arrives for you, or a counsellor joins your room |
| `/user/queue/status` | `MessageStatusUpdate` | One of your sent messages changed to `DELIVERED` or `READ` |
| `/user/queue/typing` | `TypingNotification` | The other participant started or stopped typing |
| `/user/queue/presence` | `PresenceEvent` | A contact came online or went offline |
| `/topic/open-rooms` | `ChatMessageDto` | A new message in an OPEN (unassigned) room (counsellors only) |

> **Note:** The STOMP user prefix is `/user`. Spring automatically appends the principal ID, so you subscribe to `/user/queue/messages` (not `/user/{id}/queue/messages`).

---

## Authentication

All REST requests require:

```
Authorization: Bearer <access_token>
```

Obtain a token via `POST /api/v1/auth/login`:

```json
{
  "userName": "user@example.com",
  "password": "yourpassword",
  "institutionId": "<institution-uuid>"
}
```

The response `data.accessToken` is the Bearer token.

---

## Testing with Postman

### 1. Set Up Environment

Create a Postman environment with:

| Variable | Value |
|---|---|
| `base_url` | `http://localhost:8080` |
| `user_token` | _(set after user login)_ |
| `counsellor_token` | _(set after counsellor login)_ |
| `chat_room_id` | _(set after user sends first message)_ |

---

### 2. Login as User

```
POST {{base_url}}/api/v1/auth/login
Content-Type: application/json

{
  "userName": "user@example.com",
  "password": "password123",
  "institutionId": "<institution-uuid>"
}
```

Copy `data.accessToken` → `user_token`.

---

### 3. Login as Counsellor

Same endpoint with counsellor credentials → `counsellor_token`.

---

### 4. User Sends First Message

```
POST {{base_url}}/api/v1/chat/send
Authorization: Bearer {{user_token}}
Content-Type: application/json

{
  "content": "Hi, I need someone to talk to"
}
```

No `recipientId` required. An OPEN room is created automatically.

---

### 5. Counsellor Views Open Rooms

```
GET {{base_url}}/api/v1/chat/rooms/open
Authorization: Bearer {{counsellor_token}}
```

Copy a room `id` → `chat_room_id`.

---

### 6. Counsellor Claims the Room

```
POST {{base_url}}/api/v1/chat/rooms/{{chat_room_id}}/claim
Authorization: Bearer {{counsellor_token}}
```

Room is now `ACTIVE`. The user is notified via WebSocket.

---

### 7. Counsellor Replies

```
POST {{base_url}}/api/v1/chat/send
Authorization: Bearer {{counsellor_token}}
Content-Type: application/json

{
  "chatRoomId": "{{chat_room_id}}",
  "content": "Hello, I'm here to help. What's on your mind?"
}
```

---

### 8. User Replies

```
POST {{base_url}}/api/v1/chat/send
Authorization: Bearer {{user_token}}
Content-Type: application/json

{
  "chatRoomId": "{{chat_room_id}}",
  "content": "I've been feeling really anxious lately..."
}
```

---

### 9. Read Conversation

```
GET {{base_url}}/api/v1/chat/rooms/{{chat_room_id}}/messages
Authorization: Bearer {{user_token}}
```

---

### 10. Mark as Read

```
PUT {{base_url}}/api/v1/chat/rooms/{{chat_room_id}}/read
Authorization: Bearer {{user_token}}
```

This marks all messages sent by the other participant as `READ` and pushes a `MessageStatusUpdate` to the original sender in real time.

---

### 11. Observe Message Status in Responses

Every `POST /send` response and every message in a `GET /rooms/{id}/messages` response includes:

```json
{
  "messageStatus": "SENT"
}
```

Possible values at rest:

| Value | When |
|---|---|
| `SENT` | Recipient was offline when the message was sent |
| `DELIVERED` | Recipient was online at send time, or sent a `/app/chat.delivered` ACK |
| `READ` | `PUT /rooms/{id}/read` was called by the recipient |

> **Delivery ACK via STOMP only:** Marking a message as `DELIVERED` requires the recipient to send `/app/chat.delivered` over the WebSocket connection. This cannot be done through Postman REST. Use the [browser test snippet](#testing-websocket-in-browser) to test this flow.

---

## Testing WebSocket in Browser

Postman does not natively support STOMP over SockJS. Use the following HTML snippet instead — open it directly in a browser:

```html
<!DOCTYPE html>
<html>
<head>
  <title>Trustline Chat Test</title>
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
</head>
<body>
<script>
  // Paste JWT token here
  const TOKEN = "your_jwt_token_here";
  // Set a chatRoomId to test typing / delivery in a specific room
  const CHAT_ROOM_ID = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";

  const socket = new SockJS("http://localhost:8080/ws");
  const client = Stomp.over(socket);

  client.connect({ Authorization: "Bearer " + TOKEN }, function () {
    console.log("✅ Connected");

    // 1. Receive personal messages
    client.subscribe("/user/queue/messages", function (msg) {
      const data = JSON.parse(msg.body);
      console.log("📨 Message received:", data);

      // Automatically send a delivery ACK for every incoming message
      if (data.id) {
        client.send("/app/chat.delivered", {}, JSON.stringify({ messageId: data.id }));
        console.log("✔ Delivery ACK sent for message", data.id);
      }
    });

    // 2. Receive message status updates (DELIVERED / READ) for messages you sent
    client.subscribe("/user/queue/status", function (msg) {
      console.log("📬 Status update:", JSON.parse(msg.body));
    });

    // 3. Receive typing notifications from the other participant
    client.subscribe("/user/queue/typing", function (msg) {
      const { senderId, typing } = JSON.parse(msg.body);
      console.log(typing ? `✏️  ${senderId} is typing...` : `🛑 ${senderId} stopped typing`);
    });

    // 4. Receive online/offline presence events for contacts
    client.subscribe("/user/queue/presence", function (msg) {
      const { userId, online } = JSON.parse(msg.body);
      console.log(online ? `🟢 ${userId} is online` : `🔴 ${userId} is offline`);
    });

    // 5. Counsellors: receive broadcasts for OPEN (unassigned) rooms
    client.subscribe("/topic/open-rooms", function (msg) {
      console.log("📋 Open room message:", JSON.parse(msg.body));
    });

    // --- Send a message ---
    client.send("/app/chat.send", {}, JSON.stringify({
      chatRoomId: CHAT_ROOM_ID,
      content: "Hello from WebSocket"
    }));

    // --- Simulate typing indicator ---
    client.send("/app/chat.typing", {}, JSON.stringify({
      chatRoomId: CHAT_ROOM_ID,
      typing: true
    }));
    setTimeout(() => {
      client.send("/app/chat.typing", {}, JSON.stringify({
        chatRoomId: CHAT_ROOM_ID,
        typing: false
      }));
    }, 3000);
  });
</script>
</body>
</html>
```

Open the browser console to see all real-time events as they arrive.
