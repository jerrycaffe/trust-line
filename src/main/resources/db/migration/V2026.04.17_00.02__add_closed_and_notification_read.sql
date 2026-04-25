-- Add closed column to cases table
ALTER TABLE cases ADD COLUMN IF NOT EXISTS closed BOOLEAN NOT NULL DEFAULT FALSE;

-- Add is_read column to notifications table
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT FALSE;
