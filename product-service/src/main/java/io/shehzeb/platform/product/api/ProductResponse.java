package io.shehzeb.platform.product.api;

import io.shehzeb.platform.product.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API representation of a product returned to clients. Keeps the persistence entity
 * decoupled from the wire contract.
 */
public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
