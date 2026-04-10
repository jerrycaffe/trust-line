ALTER TABLE users
ADD COLUMN institution_id UUID,
ADD CONSTRAINT fk_users_institution_id
FOREIGN KEY (institution_id)
REFERENCES institutions(id);