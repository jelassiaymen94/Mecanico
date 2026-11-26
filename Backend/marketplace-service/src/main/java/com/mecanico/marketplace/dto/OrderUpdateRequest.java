package com.mecanico.marketplace.dto;

import com.mecanico.marketplace.entity.BuyerType;
import com.mecanico.marketplace.entity.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderUpdateRequest(
        @NotNull BuyerType buyerType,
        @NotNull @Min(1) Integer quantity,
        @NotNull OrderStatus status
) {
}
