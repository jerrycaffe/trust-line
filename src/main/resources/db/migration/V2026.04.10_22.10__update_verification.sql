ALTER TABLE verifications
ADD CONSTRAINT fk_verifications_user_id FOREIGN KEY (user_id) REFERENCES users (id);
