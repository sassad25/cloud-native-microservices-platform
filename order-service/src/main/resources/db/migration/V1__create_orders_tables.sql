-- Orders schema
CREATE TABLE orders (
    id          UUID PRIMARY KEY,
    customer_id VARCHAR(255)  NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    total       NUMERIC(14,2) NOT NULL DEFAULT 0,
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE order_lines (
    id         UUID PRIMARY KEY,
    order_id   UUID          NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID          NOT NULL,
    quantity   INTEGER       NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0)
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_order_lines_order ON order_lines (order_id);
