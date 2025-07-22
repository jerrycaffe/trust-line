CREATE TABLE IF NOT EXISTS verifications(
   id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    pin character varying NOT NULL,
    message_id character varying,
    mode character varying NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
    );