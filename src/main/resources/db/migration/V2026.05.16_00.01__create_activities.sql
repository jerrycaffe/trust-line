CREATE TABLE IF NOT EXISTS activities (
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    institution_id UUID NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    grade_type VARCHAR(32) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_activities_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE CASCADE,
    CONSTRAINT fk_activities_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_activities_institution_name UNIQUE (institution_id, name)
);

CREATE INDEX IF NOT EXISTS activities_institution_idx ON activities (institution_id);
CREATE INDEX IF NOT EXISTS activities_created_by_idx ON activities (created_by);
CREATE INDEX IF NOT EXISTS activities_grade_type_idx ON activities (grade_type);

CREATE TABLE IF NOT EXISTS user_activity_entries (
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    activity_id UUID NOT NULL,
    user_id UUID NOT NULL,
    institution_id UUID NOT NULL,
    value NUMERIC(15, 4) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_uae_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    CONSTRAINT fk_uae_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_uae_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS uae_activity_idx ON user_activity_entries (activity_id);
CREATE INDEX IF NOT EXISTS uae_user_idx ON user_activity_entries (user_id);
CREATE INDEX IF NOT EXISTS uae_institution_idx ON user_activity_entries (institution_id);
CREATE INDEX IF NOT EXISTS uae_created_at_idx ON user_activity_entries (created_at);
CREATE INDEX IF NOT EXISTS uae_user_activity_idx ON user_activity_entries (user_id, activity_id);

INSERT INTO permissions (id, name, description, institution_id, created_at, updated_at)
SELECT uuid_generate_v4(), 'MANAGE_ACTIVITIES', 'Manage activities and view all user entries', i.id, NOW(), NOW()
FROM institutions i
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.institution_id = i.id AND p.name = 'MANAGE_ACTIVITIES'
);
