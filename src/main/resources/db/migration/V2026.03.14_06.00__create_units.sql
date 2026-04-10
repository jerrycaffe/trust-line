CREATE TABLE IF NOT EXISTS units(
   id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
   institution_id UUID,
   name CHARACTER varying NOT NULL,
   created_at TIMESTAMP WITH TIME ZONE,
   updated_at TIMESTAMP WITH TIME ZONE,
   CONSTRAINT fk_units_institution_id FOREIGN KEY (institution_id)  REFERENCES institutions(id) ON DELETE CASCADE
);

