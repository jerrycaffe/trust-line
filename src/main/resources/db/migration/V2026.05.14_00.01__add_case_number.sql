-- Add case_number column to cases table
ALTER TABLE cases ADD COLUMN IF NOT EXISTS case_number VARCHAR(10) UNIQUE NOT NULL DEFAULT '';

-- Create index on case_number for faster lookups
CREATE INDEX IF NOT EXISTS cases_case_number_idx ON cases(case_number);
