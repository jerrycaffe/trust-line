-- Add next_unit column to cases table
ALTER TABLE cases ADD COLUMN IF NOT EXISTS next_unit UUID;
ALTER TABLE cases ADD CONSTRAINT fk_cases_next_unit FOREIGN KEY (next_unit) REFERENCES units(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS cases_next_unit_idx ON cases (next_unit);
