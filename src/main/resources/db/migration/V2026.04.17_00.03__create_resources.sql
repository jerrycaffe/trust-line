-- Add missing columns to existing resources table
ALTER TABLE resources ADD COLUMN IF NOT EXISTS institution_id UUID;
ALTER TABLE resources ADD CONSTRAINT fk_resources_institution_id FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS resources_institution_id_idx ON resources (institution_id);

ALTER TABLE resources ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
