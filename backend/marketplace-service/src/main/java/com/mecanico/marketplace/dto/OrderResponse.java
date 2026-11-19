package com.mecanico.marketplace.dto;

import com.mecanico.marketplace.entity.BuyerType;
import com.mecanico.marketplace.entity.Order;
import com.mecanico.marketplace.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long listingId,
        String listingTitle,
        String buyerId,
        BuyerType buyerType,
        Integer quantity,
        BigDecimal totalPrice,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getListing().getId(),
                order.getListing().getTitle(),
                order.getBuyerId(),
                order.getBuyerType(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
