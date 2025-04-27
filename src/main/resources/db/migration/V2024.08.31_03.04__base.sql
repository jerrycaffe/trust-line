CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users(
   id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    email CHARACTER varying UNIQUE NOT NULL,
    phone_number CHARACTER varying UNIQUE,
    account_verified BOOLEAN DEFAULT false,
    auth_provider CHARACTER varying,
    google_id CHARACTER varying,
     deleted BOOLEAN default false,
     first_name CHARACTER VARYING,
     last_name CHARACTER VARYING,
     status CHARACTER VARYING,
     gender CHARACTER VARYING,
     password CHARACTER VARYING,
     profile_image_url CHARACTER VARYING,
     created_at TIMESTAMP WITH TIME ZONE,
     updated_at TIMESTAMP WITH TIME ZONE
    );

    CREATE INDEX IF NOT EXISTS users_email_idx ON users (email);

    CREATE TABLE IF NOT EXISTS roles
    (
        id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
        name CHARACTER VARYING UNIQUE NOT NULL,
        description CHARACTER VARYING NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE,
        updated_at TIMESTAMP WITH TIME ZONE
    );

    CREATE TABLE IF NOT EXISTS permissions
    (
        id UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
        name CHARACTER VARYING UNIQUE NOT NULL,
        description CHARACTER VARYING NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE,
        updated_at TIMESTAMP WITH TIME ZONE
    );

    CREATE TABLE IF NOT EXISTS users_roles
    (
        user_id UUID NOT NULL,
        role_id UUID NOT NULL,
        CONSTRAINT pk_users_roles_user_id_role_id PRIMARY KEY (user_id, role_id),
        CONSTRAINT fk_users_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT fk_users_roles_role_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS roles_permissions
    (
        role_id UUID NOT NULL,
        permission_id UUID NOT NULL,
        CONSTRAINT pk_roles_permissions_role_id_permission_id PRIMARY KEY (role_id, permission_id),
        CONSTRAINT fk_roles_permissions_permission_id FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
        CONSTRAINT fk_roles_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );