package com.mecanico.vehiclepassport.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Event contracts (JSON) exchanged on the shared {@code mecanico.events} topic exchange. */
public final class Events {

    public static final String EXCHANGE = "mecanico.events";

    public static final String DOCUMENT_EXPIRING = "document.expiring";
    public static final String REQUEST_COMPLETED = "request.completed";

    private Events() {
    }

    /** Published by MS1 at J-30 and J-7 before a document expires. */
    public record DocumentExpiringEvent(
            Long vehicleId,
            Long ownerId,
            String plate,
            Long documentId,
            String documentType,
            String documentNumber,
            LocalDate expiryDate,
            long daysLeft,
            LocalDateTime occurredAt) {
    }

    /** Published by MS2 when a service request reaches COMPLETED. */
    public record RequestCompletedEvent(
            Long requestId,
            Long customerId,
            Long providerId,
            Long vehicleId,
            String category,
            String title,
            String description,
            Integer mileage,
            BigDecimal finalPrice,
            LocalDateTime completedAt) {
    }
}
