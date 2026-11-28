package com.mecanico.marketplace.service;

import com.mecanico.marketplace.dto.OrderCreateRequest;
import com.mecanico.marketplace.dto.OrderResponse;
import com.mecanico.marketplace.dto.OrderUpdateRequest;
import com.mecanico.marketplace.entity.BuyerType;
import com.mecanico.marketplace.entity.ItemCondition;
import com.mecanico.marketplace.entity.Listing;
import com.mecanico.marketplace.entity.ListingCategory;
import com.mecanico.marketplace.entity.Order;
import com.mecanico.marketplace.entity.OrderStatus;
import com.mecanico.marketplace.exception.ConflictException;
import com.mecanico.marketplace.exception.ResourceNotFoundException;
import com.mecanico.marketplace.repository.ListingRepository;
import com.mecanico.marketplace.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private com.mecanico.marketplace.messaging.MarketplaceEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createShouldComputeTotalPriceAndPendingStatus() {
        Listing listing = listing(1L, "Engine", "100.00", 5);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderCreateRequest request = new OrderCreateRequest(1L, "buyer-1", BuyerType.INDIVIDUAL, 2);
        OrderResponse response = orderService.create(request);

        assertThat(response.listingId()).isEqualTo(1L);
        assertThat(response.buyerId()).isEqualTo("buyer-1");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.totalPrice()).isEqualByComparingTo("200.00");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void createShouldRejectQuantityAboveAvailableStock() {
        Listing listing = listing(1L, "Engine", "100.00", 1);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        OrderCreateRequest request = new OrderCreateRequest(1L, "buyer-1", BuyerType.INDIVIDUAL, 2);

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("quantity");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateShouldRecomputeTotalPriceAndChangeStatus() {
        Listing listing = listing(1L, "Engine", "100.00", 5);
        Order order = new Order(listing, "buyer-1", BuyerType.INDIVIDUAL, 1, new BigDecimal("100.00"));
        order.setStatus(OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "id", 10L);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderUpdateRequest request = new OrderUpdateRequest(BuyerType.PROFESSIONAL, 3, OrderStatus.CONFIRMED);
        OrderResponse response = orderService.update(10L, request);

        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.totalPrice()).isEqualByComparingTo("300.00");
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.buyerType()).isEqualTo(BuyerType.PROFESSIONAL);
    }

    @Test
    void getShouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    private Listing listing(Long id, String title, String price, int quantity) {
        Listing listing = new Listing(
                "seller-1",
                title,
                "Part description",
                ListingCategory.ENGINE,
                "Renault",
                "Clio 3",
                ItemCondition.USED,
                new BigDecimal(price),
                quantity,
                "Tunis"
        );
        ReflectionTestUtils.setField(listing, "id", id);
        return listing;
    }
}
