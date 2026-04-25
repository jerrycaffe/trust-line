-- Create incident_types table
CREATE TABLE IF NOT EXISTS incident_types (
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    name CHARACTER VARYING(255) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    institution_id UUID NOT NULL,
    created_by UUID,
    steps INTEGER DEFAULT 1,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_incident_types_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_incident_types_institution_id FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS incident_types_created_by_idx ON incident_types(created_by);
CREATE INDEX IF NOT EXISTS incident_types_institution_idx ON incident_types(institution_id);

CREATE TABLE IF NOT EXISTS incident_type_units(
    incident_type_id UUID NOT NULL,
    unit_id UUID NOT NULL,
    purpose TEXT,
    CONSTRAINT pk_incident_type_units_incident_type_id_unit_id PRIMARY KEY (incident_type_id, unit_id),
    CONSTRAINT fk_incident_type_units_incident_type_id FOREIGN KEY (incident_type_id) REFERENCES incident_types(id) ON DELETE CASCADE,
    CONSTRAINT fk_incident_type_unit_id FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE
);

-- Create cases table
CREATE TABLE IF NOT EXISTS cases (
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    incident_type_id UUID NOT NULL,
    date_of_incident TIMESTAMP WITH TIME ZONE NOT NULL,
    institution_id UUID NOT NULL,
    location TEXT NOT NULL,
    description TEXT NOT NULL,
    reported_by UUID NOT NULL,
    status TEXT,
    current_unit UUID,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_cases_incident_type_id FOREIGN KEY (incident_type_id) REFERENCES incident_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_cases_reported_by FOREIGN KEY (reported_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_cases_current_unit FOREIGN KEY (current_unit) REFERENCES units(id) ON DELETE SET NULL,
    CONSTRAINT fk_cases_institution_id FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS cases_incident_type_id_idx ON cases (incident_type_id);
CREATE INDEX IF NOT EXISTS cases_reported_by_idx ON cases (reported_by);
CREATE INDEX IF NOT EXISTS cases_current_unit_idx ON cases (current_unit);
CREATE INDEX IF NOT EXISTS cases_date_of_incident_idx ON cases (date_of_incident);
CREATE INDEX IF NOT EXISTS incident_types_institution_id_idx ON cases (institution_id);


CREATE TABLE IF NOT EXISTS comments(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    comment TEXT NOT NULL,
    case_id UUID NOT NULL,
    commenter UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_comments_case_id FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_commenter FOREIGN KEY (commenter) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS comments_case_id_idx ON comments (case_id);



CREATE TABLE IF NOT EXISTS file_uploads(
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  upload_url TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE,
  updated_at TIMESTAMP WITH TIME ZONE
);


CREATE TABLE IF NOT EXISTS case_file_uploads(
  id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
  file_upload_id UUID NOT NULL,
  case_id UUID,
  created_at TIMESTAMP WITH TIME ZONE,
  updated_at TIMESTAMP WITH TIME ZONE,
  CONSTRAINT fk_case_file_uploads_case_id FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE,
  CONSTRAINT fk_case_file_uploads_file_upload_id FOREIGN KEY (file_upload_id) REFERENCES file_uploads(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS case_file_uploads_file_upload_id_idx ON case_file_uploads (file_upload_id);
CREATE INDEX IF NOT EXISTS case_file_uploads_case_id_id_idx ON case_file_uploads (case_id);


CREATE TABLE IF NOT EXISTS notifications(
    id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    topic TEXT NOT NULL,
    message TEXT NOT NULL,
    destination_id UUID,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_notifications_destination_id FOREIGN KEY (destination_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS notifications_destination_id_idx ON notifications (destination_id);
