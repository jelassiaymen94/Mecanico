package com.mecanico.vehiclepassport.dto;

import com.mecanico.vehiclepassport.entity.DocumentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DocumentDtos {

    private DocumentDtos() {
    }

    public record VehicleDocumentRequest(
            @jakarta.validation.constraints.NotNull DocumentType type,
            @jakarta.validation.constraints.Size(max = 80) String number,
            @jakarta.validation.constraints.Size(max = 150) String issuer,
            @jakarta.validation.constraints.PastOrPresent LocalDate issueDate,
            @jakarta.validation.constraints.NotNull LocalDate expiryDate,
            @jakarta.validation.constraints.Size(max = 500) String fileUrl,
            @jakarta.validation.constraints.DecimalMin("0.0") BigDecimal cost,
            @jakarta.validation.constraints.Size(max = 2000) String notes,
            Boolean active
    ) {
    }

    public record VehicleDocumentResponse(
            Long id,
            Long vehicleId,
            DocumentType type,
            String number,
            String issuer,
            LocalDate issueDate,
            LocalDate expiryDate,
            String fileUrl,
            BigDecimal cost,
            String notes,
            boolean notification30Sent,
            boolean notification7Sent,
            boolean active,
            long daysUntilExpiry,
            boolean expired,
            boolean expiringSoon,
            LocalDateTime createdAt
    ) {
    }
}
