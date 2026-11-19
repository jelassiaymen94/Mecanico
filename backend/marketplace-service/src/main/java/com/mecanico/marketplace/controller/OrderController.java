package com.mecanico.marketplace.controller;

import com.mecanico.marketplace.dto.OrderCreateRequest;
import com.mecanico.marketplace.dto.OrderResponse;
import com.mecanico.marketplace.dto.OrderUpdateRequest;
import com.mecanico.marketplace.dto.PageResponse;
import com.mecanico.marketplace.entity.OrderStatus;
import com.mecanico.marketplace.service.OrderService;
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
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Purchase order CRUD")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an order")
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @GetMapping
    @Operation(summary = "List orders with pagination and filters")
    public PageResponse<OrderResponse> list(
            @RequestParam(required = false) String buyerId,
            @RequestParam(required = false) Long listingId,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return orderService.list(buyerId, listingId, status, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an order")
    public OrderResponse update(@PathVariable Long id, @Valid @RequestBody OrderUpdateRequest request) {
        return orderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an order")
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }
}
