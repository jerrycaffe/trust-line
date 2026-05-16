-- Enforce institution-scoped uniqueness for users.
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_phone_number_key;

DROP INDEX IF EXISTS users_email_idx;
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_institution_email
    ON users (institution_id, email);
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_institution_phone
    ON users (institution_id, phone_number)
    WHERE phone_number IS NOT NULL;

-- Enforce institution-scoped uniqueness for incident type names.
ALTER TABLE incident_types DROP CONSTRAINT IF EXISTS incident_types_name_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_incident_types_institution_name
    ON incident_types (institution_id, name)
    WHERE deleted = FALSE;

-- Seed dynamic action permissions globally.
INSERT INTO permissions(name, description, created_at, updated_at)
VALUES ('MANAGE_USERS', 'Can invite users and change user roles', now(), now()),
       ('MANAGE_ROLES', 'Can create roles and assign permissions to roles', now(), now()),
       ('MANAGE_PERMISSIONS', 'Can create and list permissions', now(), now()),
       ('MANAGE_CASES', 'Can list all cases, comment, close and reopen cases', now(), now()),
       ('MANAGE_INCIDENT_TYPES', 'Can create, update and delete incident types', now(), now()),
       ('MANAGE_RESOURCES', 'Can create, update and delete resources', now(), now()),
       ('MANAGE_NOTIFICATIONS', 'Can create notifications', now(), now())
ON CONFLICT DO NOTHING;

-- Grant dynamic permissions to the default Administrator role.
INSERT INTO roles_permissions(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'MANAGE_USERS',
    'MANAGE_ROLES',
    'MANAGE_PERMISSIONS',
    'MANAGE_CASES',
    'MANAGE_INCIDENT_TYPES',
    'MANAGE_RESOURCES',
    'MANAGE_NOTIFICATIONS'
)
WHERE r.name = 'Administrator' AND r.institution_id IS NULL
ON CONFLICT DO NOTHING;
