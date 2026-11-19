package com.mecanico.marketplace.dto;

import com.mecanico.marketplace.entity.ItemCondition;
import com.mecanico.marketplace.entity.Listing;
import com.mecanico.marketplace.entity.ListingCategory;
import com.mecanico.marketplace.entity.ListingStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ListingResponse(
        Long id,
        String sellerId,
        String title,
        String description,
        ListingCategory category,
        String brand,
        String compatibleModels,
        ItemCondition condition,
        BigDecimal price,
        Integer quantity,
        String city,
        ListingStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ListingResponse from(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getSellerId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getCategory(),
                listing.getBrand(),
                listing.getCompatibleModels(),
                listing.getCondition(),
                listing.getPrice(),
                listing.getQuantity(),
                listing.getCity(),
                listing.getStatus(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}
