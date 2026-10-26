package io.shehzeb.platform.order.domain;

import java.util.UUID;

/** Thrown when an order cannot be located by id. Mapped to HTTP 404. */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID id) {
        super("Order not found: " + id);
    }
}
