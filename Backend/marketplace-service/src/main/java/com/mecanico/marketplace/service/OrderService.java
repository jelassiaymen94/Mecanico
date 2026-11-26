package com.mecanico.marketplace.service;

import com.mecanico.marketplace.dto.OrderCreateRequest;
import com.mecanico.marketplace.dto.OrderResponse;
import com.mecanico.marketplace.dto.OrderUpdateRequest;
import com.mecanico.marketplace.dto.PageResponse;
import com.mecanico.marketplace.entity.Listing;
import com.mecanico.marketplace.entity.Order;
import com.mecanico.marketplace.entity.OrderStatus;
import com.mecanico.marketplace.exception.ConflictException;
import com.mecanico.marketplace.exception.ResourceNotFoundException;
import com.mecanico.marketplace.repository.ListingRepository;
import com.mecanico.marketplace.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;

    public OrderService(OrderRepository orderRepository, ListingRepository listingRepository) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
    }

    public OrderResponse create(OrderCreateRequest request) {
        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing " + request.listingId() + " not found"));

        validateQuantity(listing, request.quantity());
        BigDecimal totalPrice = computeTotalPrice(listing, request.quantity());

        Order order = new Order(listing, request.buyerId(), request.buyerType(), request.quantity(), totalPrice);
        order.setStatus(OrderStatus.PENDING);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> list(
            String buyerId,
            Long listingId,
            OrderStatus status,
            Pageable pageable
    ) {
        Specification<Order> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (buyerId != null && !buyerId.isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("buyerId"), buyerId));
        }
        if (listingId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("listing").get("id"), listingId));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }

        Page<Order> page = orderRepository.findAll(specification, pageable);
        return PageResponse.from(page.map(OrderResponse::from));
    }

    public OrderResponse update(Long id, OrderUpdateRequest request) {
        Order order = findById(id);
        validateQuantity(order.getListing(), request.quantity());

        order.setBuyerType(request.buyerType());
        order.setQuantity(request.quantity());
        order.setTotalPrice(computeTotalPrice(order.getListing(), request.quantity()));
        order.setStatus(request.status());
        return OrderResponse.from(orderRepository.save(order));
    }

    public void delete(Long id) {
        Order order = findById(id);
        orderRepository.delete(order);
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found"));
    }

    private void validateQuantity(Listing listing, Integer quantity) {
        if (quantity > listing.getQuantity()) {
            throw new ConflictException("Order quantity exceeds the available listing quantity");
        }
    }

    private BigDecimal computeTotalPrice(Listing listing, Integer quantity) {
        return listing.getPrice()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
