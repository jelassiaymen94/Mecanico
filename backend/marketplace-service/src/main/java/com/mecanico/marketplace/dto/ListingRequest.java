package com.mecanico.marketplace.dto;

import com.mecanico.marketplace.entity.ItemCondition;
import com.mecanico.marketplace.entity.ListingCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ListingRequest(
        @NotBlank @Size(max = 64) String sellerId,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 4000) String description,
        @NotNull ListingCategory category,
        @Size(max = 120) String brand,
        @Size(max = 255) String compatibleModels,
        @NotNull ItemCondition condition,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull @Min(0) Integer quantity,
        @NotBlank @Size(max = 120) String city
) {
}
