CREATE TABLE product (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    name VARCHAR(12) NOT NULL,
    description VARCHAR(100),
    product_status VARCHAR(20) NOT NULL
);

CREATE TABLE product_option (
    option_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(10) NOT NULL,
    CONSTRAINT fk_product_option_product FOREIGN KEY (product_id) REFERENCES product (product_id)
);

CREATE TABLE option_value (
    option_value_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    option_id BIGINT NOT NULL,
    name VARCHAR(10) NOT NULL,
    CONSTRAINT fk_option_value_option FOREIGN KEY (option_id) REFERENCES product_option (option_id)
);

CREATE TABLE variant (
    variant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    price INT NOT NULL,
    variant_status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES product (product_id)
);

CREATE TABLE variant_option_value (
    variant_id BIGINT NOT NULL,
    option_value_id BIGINT NOT NULL,
    CONSTRAINT fk_variant_option_value_variant FOREIGN KEY (variant_id) REFERENCES variant (variant_id),
    CONSTRAINT fk_variant_option_value_option_value FOREIGN KEY (option_value_id) REFERENCES option_value (option_value_id)
);
