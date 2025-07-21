CREATE TABLE IF NOT EXISTS verifications(
   id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    pin varying character varying NOT NULL,
    message_id varying character,
    mode varying character NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
    );