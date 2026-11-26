package com.mecanico.vehiclepassport.dto;

import com.mecanico.vehiclepassport.entity.BodyType;

import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record BrandResponse(
            Long id,
            String name,
            String slug,
            String country,
            String logoUrl,
            long modelCount
    ) {
    }

    public record ModelResponse(
            Long id,
            Long brandId,
            String brandName,
            String name,
            BodyType bodyType,
            Integer yearFrom,
            Integer yearTo
    ) {
    }

    public record CatalogResponse(
            List<BrandResponse> brands,
            long totalBrands,
            long totalModels
    ) {
    }
}
