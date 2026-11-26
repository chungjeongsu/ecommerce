CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(6) NOT NULL,
    email VARCHAR(320) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    user_status VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_users_email_role UNIQUE (email, user_role),
    CONSTRAINT chk_users_name_length CHECK (CHAR_LENGTH(name) BETWEEN 2 AND 6),
    CONSTRAINT chk_users_role CHECK (user_role IN ('BUYER', 'SELLER')),
    CONSTRAINT chk_users_status CHECK (user_status IN ('ACTIVE', 'WITHDRAWN'))
);
