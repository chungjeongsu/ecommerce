CREATE TABLE inventory (
    inventory_id BIGINT NOT NULL AUTO_INCREMENT,
    variant_id BIGINT NOT NULL,
    total_quantity INT NOT NULL,
    inventory_status VARCHAR(20) NOT NULL,
    PRIMARY KEY (inventory_id),
    CONSTRAINT uk_inventory_variant UNIQUE (variant_id),
    CONSTRAINT chk_inventory_total_quantity CHECK (total_quantity >= 0),
    CONSTRAINT chk_inventory_status CHECK (inventory_status IN ('ACTIVE', 'CLOSING', 'CLOSED'))
);

CREATE TABLE inventory_reservation (
    inventory_reservation_id BIGINT NOT NULL AUTO_INCREMENT,
    inventory_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    inventory_reservation_status VARCHAR(20) NOT NULL,
    PRIMARY KEY (inventory_reservation_id),
    CONSTRAINT uk_inventory_reservation_order_item
        UNIQUE (inventory_id, order_id, order_item_id),
    CONSTRAINT chk_inventory_reservation_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_inventory_reservation_status
        CHECK (inventory_reservation_status IN ('RESERVED', 'CONFIRMED', 'RELEASED'))
);

CREATE INDEX idx_inventory_reservation_inventory_id
    ON inventory_reservation (inventory_id);
