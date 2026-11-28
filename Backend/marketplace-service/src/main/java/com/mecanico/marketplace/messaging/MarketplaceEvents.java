package com.mecanico.marketplace.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Event contracts published by MS3 on the shared {@code mecanico.events} topic exchange. */
public final class MarketplaceEvents {

    public static final String EXCHANGE = "mecanico.events";
    public static final String ORDER_CONFIRMED = "order.confirmed";

    private MarketplaceEvents() {
    }

    /** Order accepted by the seller: consumed by MS6 Billing to issue the invoice. */
    public record OrderConfirmedEvent(
            Long orderId,
            Long listingId,
            String listingTitle,
            String sellerId,
            String buyerId,
            String buyerType,
            Integer quantity,
            BigDecimal totalPrice,
            String currency,
            LocalDateTime confirmedAt) {
    }
}
