package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.CatalogDtos;
import com.mecanico.vehiclepassport.entity.VehicleBrand;
import com.mecanico.vehiclepassport.entity.VehicleModel;
import com.mecanico.vehiclepassport.exception.ApiException;
import com.mecanico.vehiclepassport.repository.VehicleBrandRepository;
import com.mecanico.vehiclepassport.repository.VehicleModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final VehicleBrandRepository brands;
    private final VehicleModelRepository models;

    public CatalogService(VehicleBrandRepository brands, VehicleModelRepository models) {
        this.brands = brands;
        this.models = models;
    }

    @Transactional(readOnly = true)
    public CatalogDtos.CatalogResponse catalog() {
        List<CatalogDtos.BrandResponse> brandResponses = brands.findAllByActiveTrueOrderByNameAsc().stream()
                .map(brand -> toBrandResponse(brand, models.countByBrandIdAndActiveTrue(brand.getId())))
                .toList();
        return new CatalogDtos.CatalogResponse(brandResponses, brandResponses.size(), models.count());
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.BrandResponse> brands() {
        return brands.findAllByActiveTrueOrderByNameAsc().stream()
                .map(brand -> toBrandResponse(brand, models.countByBrandIdAndActiveTrue(brand.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ModelResponse> modelsByBrand(Long brandId) {
        VehicleBrand brand = brands.findByIdAndActiveTrue(brandId)
                .orElseThrow(() -> ApiException.notFound("Brand not found"));
        return models.findAllByBrandIdAndActiveTrueOrderByNameAsc(brandId).stream()
                .map(model -> toModelResponse(model))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ModelResponse> searchModels(Long brandId, String search) {
        String pattern = search == null || search.isBlank() ? null : "%" + search.trim().toLowerCase() + "%";
        return models.search(brandId, pattern).stream().map(this::toModelResponse).toList();
    }

    private CatalogDtos.BrandResponse toBrandResponse(VehicleBrand brand, long modelCount) {
        return new CatalogDtos.BrandResponse(brand.getId(), brand.getName(), brand.getSlug(),
                brand.getCountry(), brand.getLogoUrl(), modelCount);
    }

    private CatalogDtos.ModelResponse toModelResponse(VehicleModel model) {
        return new CatalogDtos.ModelResponse(model.getId(), model.getBrand().getId(), model.getBrand().getName(),
                model.getName(), model.getBodyType(), model.getYearFrom(), model.getYearTo());
    }
}
