-- WebAuthn User Entities Table
CREATE TABLE IF NOT EXISTS user_entities (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL
);

-- WebAuthn Credentials Table
CREATE TABLE IF NOT EXISTS user_credentials (
    id VARCHAR(255) DEFAULT RANDOM_UUID() PRIMARY KEY,
    user_entity_user_id VARCHAR(255) NOT NULL,
    credential_id VARCHAR(1024) NOT NULL UNIQUE,
    public_key TEXT NOT NULL,
    signature_count BIGINT NOT NULL,
    public_key_credential_type VARCHAR(32) NOT NULL,
    created TIMESTAMP NOT NULL,
    last_used TIMESTAMP,
    label VARCHAR(512),
    backup_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    backup_state BOOLEAN NOT NULL DEFAULT FALSE,
    uv_initialized BOOLEAN NOT NULL DEFAULT FALSE,
    authenticator_transports VARCHAR(512),
    attestation_object VARBINARY(1024),
    attestation_client_data_json VARBINARY(1024),
    FOREIGN KEY (user_entity_user_id) REFERENCES user_entities(id)
);

-- Application Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
