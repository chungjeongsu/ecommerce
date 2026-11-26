CREATE TABLE payment (
    payment_id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    pg_order_id VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    pg_payment_key VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
    pg_approval_idempotency_key VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (payment_id),
    CONSTRAINT uk_payment_pg_order_id UNIQUE (pg_order_id),
    CONSTRAINT uk_payment_pg_approval_idempotency_key UNIQUE (pg_approval_idempotency_key),
    CONSTRAINT uk_payment_pg_payment_key UNIQUE (pg_payment_key),
    CONSTRAINT chk_payment_amount CHECK (amount >= 0),
    CONSTRAINT chk_payment_status
        CHECK (payment_status IN ('READY', 'IN_PROGRESS', 'UNKNOWN', 'DONE', 'ABORTED', 'EXPIRED')),
    CONSTRAINT chk_payment_expiration CHECK (expires_at > created_at)
);

CREATE INDEX idx_payment_order_status
    ON payment (order_id, payment_status);
