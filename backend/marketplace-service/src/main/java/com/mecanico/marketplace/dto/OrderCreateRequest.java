package com.mecanico.marketplace.dto;

import com.mecanico.marketplace.entity.BuyerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderCreateRequest(
        @NotNull Long listingId,
        @NotBlank @Size(max = 64) String buyerId,
        @NotNull BuyerType buyerType,
        @NotNull @Min(1) Integer quantity
) {
}
