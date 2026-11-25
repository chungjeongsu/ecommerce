CREATE TABLE orders (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    order_code BINARY(16) NOT NULL,
    buyer_id BIGINT NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (order_id),
    CONSTRAINT uk_orders_order_code UNIQUE (order_code),
    CONSTRAINT chk_orders_status
        CHECK (order_status IN ('READY', 'PAID', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT chk_orders_expiration CHECK (expires_at > created_at)
);

CREATE TABLE order_item (
    order_item_id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    option_combination VARCHAR(1000),
    unit_price BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (order_item_id),
    CONSTRAINT uk_order_item_order_variant UNIQUE (order_id, variant_id),
    CONSTRAINT chk_order_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_item_quantity CHECK (quantity BETWEEN 1 AND 100)
);

CREATE INDEX idx_orders_status_expires_at
    ON orders (order_status, expires_at);
