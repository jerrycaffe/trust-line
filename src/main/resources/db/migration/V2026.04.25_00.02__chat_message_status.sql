-- Add message delivery status: SENT (saved, recipient offline), DELIVERED (recipient got it), READ (recipient opened it)
ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS message_status VARCHAR(20) NOT NULL DEFAULT 'SENT';

-- Migrate existing is_read=true rows to READ status
UPDATE chat_messages SET message_status = 'READ' WHERE is_read = true;

CREATE INDEX IF NOT EXISTS chat_messages_status_idx ON chat_messages (message_status);
