package io.shehzeb.platform.product.domain;

import java.util.UUID;

/**
 * Thrown when a product cannot be located by id. Mapped to HTTP 404 by the global handler.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID id) {
        super("Product not found: " + id);
    }
}
