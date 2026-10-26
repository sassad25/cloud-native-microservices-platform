package io.shehzeb.platform.order.api;

import io.shehzeb.platform.order.domain.Order;
import io.shehzeb.platform.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** API representation of an order. */
public record OrderResponse(
        UUID id,
        String customerId,
        OrderStatus status,
        BigDecimal total,
        List<Line> lines,
        Instant createdAt
) {
    public record Line(UUID productId, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public static OrderResponse from(Order order) {
        List<Line> lines = order.getLines().stream()
                .map(l -> new Line(l.getProductId(), l.getQuantity(), l.getUnitPrice(), l.getLineTotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getStatus(),
                order.getTotal(), lines, order.getCreatedAt());
    }
}
