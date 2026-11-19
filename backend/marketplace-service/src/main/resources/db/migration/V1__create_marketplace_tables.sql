CREATE TABLE listing (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seller_id VARCHAR(64) NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    brand VARCHAR(120),
    compatible_models VARCHAR(255),
    item_condition VARCHAR(30) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    city VARCHAR(120) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_listing_seller_id (seller_id),
    INDEX idx_listing_city (city),
    INDEX idx_listing_status (status),
    INDEX idx_listing_category (category)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    listing_id BIGINT NOT NULL,
    buyer_id VARCHAR(64) NOT NULL,
    buyer_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_orders_buyer_id (buyer_id),
    INDEX idx_orders_listing_id (listing_id),
    INDEX idx_orders_status (status),
    CONSTRAINT fk_orders_listing FOREIGN KEY (listing_id) REFERENCES listing (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
