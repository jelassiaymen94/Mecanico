package com.mecanico.vehiclepassport.controller;

import com.mecanico.vehiclepassport.dto.CatalogDtos;
import com.mecanico.vehiclepassport.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public CatalogDtos.CatalogResponse catalog() {
        return catalogService.catalog();
    }

    @GetMapping("/brands")
    public List<CatalogDtos.BrandResponse> brands() {
        return catalogService.brands();
    }

    @GetMapping("/brands/{brandId}/models")
    public List<CatalogDtos.ModelResponse> models(@PathVariable Long brandId) {
        return catalogService.modelsByBrand(brandId);
    }

    @GetMapping("/models")
    public List<CatalogDtos.ModelResponse> searchModels(@RequestParam(required = false) Long brandId,
                                                        @RequestParam(required = false) String search) {
        return catalogService.searchModels(brandId, search);
    }
}
