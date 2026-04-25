INSERT INTO roles(name, description, created_at, updated_at)
VALUES ('User', 'User who report cases.', now(), now()),
       ('Administrator', 'Admin who attends or operates on cases', now(), now())
ON CONFLICT DO NOTHING;

INSERT INTO permissions(name, description, created_at, updated_at)
VALUES ('VIEW_ALL_USERS', 'Can view all users', now(), now()),
       ('CREATE_ADMIN_USER', 'Can create admin users', now(), now())
ON CONFLICT DO NOTHING;


INSERT INTO roles_permissions(role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'Administrator'), id
FROM permissions
WHERE name = 'VIEW_ALL_USERS'
ON CONFLICT DO NOTHING;

INSERT INTO roles_permissions(role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'Administrator'), id
FROM permissions
WHERE name = 'CREATE_ADMIN_USER'
ON CONFLICT DO NOTHING;