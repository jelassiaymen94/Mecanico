package com.mecanico.marketplace.controller;

import com.mecanico.marketplace.dto.ListingRequest;
import com.mecanico.marketplace.dto.ListingResponse;
import com.mecanico.marketplace.dto.PageResponse;
import com.mecanico.marketplace.entity.ListingCategory;
import com.mecanico.marketplace.entity.ListingStatus;
import com.mecanico.marketplace.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listings")
@Tag(name = "Listings", description = "Part listing CRUD")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a listing")
    public ListingResponse create(@Valid @RequestBody ListingRequest request) {
        return listingService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a listing by id")
    public ListingResponse get(@PathVariable Long id) {
        return listingService.get(id);
    }

    @GetMapping
    @Operation(summary = "List listings with pagination and filters")
    public PageResponse<ListingResponse> list(
            @RequestParam(required = false) ListingCategory category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return listingService.list(category, city, status, sellerId, q, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a listing")
    public ListingResponse update(@PathVariable Long id, @Valid @RequestBody ListingRequest request) {
        return listingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a listing")
    public void delete(@PathVariable Long id) {
        listingService.delete(id);
    }
}
