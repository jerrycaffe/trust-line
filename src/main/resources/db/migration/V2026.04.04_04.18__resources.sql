CREATE TABLE IF NOT EXISTS resources(
 id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
 name TEXT NOT NULL,
 incident_type_id UUID,
 contents TEXT,
 file_upload_id UUID,
 created_by UUID NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE,
 updated_at TIMESTAMP WITH TIME ZONE,
 CONSTRAINT fk_resources_incident_type_id FOREIGN KEY (incident_type_id) REFERENCES incident_types(id) ON DELETE SET NULL,
 CONSTRAINT fk_resources_file_upload_id FOREIGN KEY (file_upload_id) REFERENCES file_uploads(id) ON DELETE SET NULL,
 CONSTRAINT fk_resources_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS resources_incident_type_id_idx ON resources (incident_type_id);
CREATE INDEX IF NOT EXISTS resources_file_upload_id_idx ON resources (file_upload_id);
CREATE INDEX IF NOT EXISTS resources_created_by_idx ON resources (created_by);