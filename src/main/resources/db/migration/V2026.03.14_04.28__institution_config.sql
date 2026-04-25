CREATE TABLE IF NOT EXISTS institutions(
   id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
   name CHARACTER varying UNIQUE NOT NULL,
   contact_number CHARACTER varying,
   address CHARACTER VARYING,
   logo_url CHARACTER VARYING,
   created_at TIMESTAMP WITH TIME ZONE,
   updated_at TIMESTAMP WITH TIME ZONE
);
