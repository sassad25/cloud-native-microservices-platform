package io.shehzeb.platform.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Payload for placing a new order. */
public record CreateOrderRequest(

        @NotBlank(message = "customerId is required")
        String customerId,

        @NotEmpty(message = "at least one line item is required")
        List<@Valid LineItem> lines
) {
    public record LineItem(
            @NotNull(message = "productId is required") UUID productId,
            @Min(value = 1, message = "quantity must be at least 1") int quantity,
            @NotNull @DecimalMin(value = "0.0", message = "unitPrice must not be negative") BigDecimal unitPrice
    ) {
    }
}
