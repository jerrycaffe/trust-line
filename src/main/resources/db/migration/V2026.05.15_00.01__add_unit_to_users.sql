ALTER TABLE users
ADD COLUMN unit_id UUID,
ADD CONSTRAINT fk_users_unit_id
FOREIGN KEY (unit_id)
REFERENCES units(id);
