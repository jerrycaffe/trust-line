-- Allow chat rooms without an assigned counsellor (open pool)
-- participant_two becomes nullable — will be set when a counsellor claims the room
ALTER TABLE chat_rooms ALTER COLUMN participant_two DROP NOT NULL;

-- Drop the old unique constraint (was based on both participants being non-null)
ALTER TABLE chat_rooms DROP CONSTRAINT IF EXISTS uq_chat_room_participants;

-- Add status column: OPEN (unassigned), ACTIVE (counsellor assigned), CLOSED
ALTER TABLE chat_rooms ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'OPEN';

-- A user can only have one OPEN room at a time (partial unique index)
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_open_chat ON chat_rooms (participant_one) WHERE status = 'OPEN';

-- Index to speed up counsellor dashboard queries for open rooms
CREATE INDEX IF NOT EXISTS chat_rooms_status_idx ON chat_rooms (status);

-- Mark existing rooms (already have two participants) as ACTIVE
UPDATE chat_rooms SET status = 'ACTIVE' WHERE participant_two IS NOT NULL;
