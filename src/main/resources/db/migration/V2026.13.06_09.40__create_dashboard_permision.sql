INSERT INTO permissions (id, name, description, institution_id, created_at, updated_at)
SELECT uuid_generate_v4(), 'DASHBOARD_METRICS', 'Dashboard metrics and view all dashboard summary', i.id, NOW(), NOW()
FROM institutions i
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.institution_id = i.id AND p.name = 'DASHBOARD_METRICS'
);