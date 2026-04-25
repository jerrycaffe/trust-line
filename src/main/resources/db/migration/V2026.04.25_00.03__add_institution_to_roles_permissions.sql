-- Add institution_id (nullable) to roles.
-- NULL = system-wide/global role; non-NULL = institution-specific role.
ALTER TABLE roles ADD COLUMN IF NOT EXISTS institution_id UUID;
ALTER TABLE roles
    ADD CONSTRAINT fk_roles_institution_id
    FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE CASCADE;

-- Replace the global unique-on-name with two targeted unique indexes.
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_roles_name_institution
    ON roles(name, institution_id) WHERE institution_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_roles_name_global
    ON roles(name) WHERE institution_id IS NULL;

-- Add institution_id (nullable) to permissions.
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS institution_id UUID;
ALTER TABLE permissions
    ADD CONSTRAINT fk_permissions_institution_id
    FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE CASCADE;

ALTER TABLE permissions DROP CONSTRAINT IF EXISTS permissions_name_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_permissions_name_institution
    ON permissions(name, institution_id) WHERE institution_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_permissions_name_global
    ON permissions(name) WHERE institution_id IS NULL;
