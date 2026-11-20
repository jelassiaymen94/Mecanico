package com.mecanico.vehiclepassport.dto;

import com.mecanico.vehiclepassport.entity.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class MaintenanceDtos {

    private MaintenanceDtos() {
    }

    public record MaintenanceRecordRequest(
            @jakarta.validation.constraints.NotNull RecordType type,
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.PastOrPresent LocalDate serviceDate,
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Min(0) Integer mileage,
            @jakarta.validation.constraints.Size(max = 2000) String description,
            @jakarta.validation.constraints.Min(0) Long providerId,
            @jakarta.validation.constraints.Size(max = 150) String providerName,
            @jakarta.validation.constraints.Min(0) Long requestId,
            @jakarta.validation.constraints.DecimalMin("0.0") BigDecimal cost,
            @jakarta.validation.constraints.Size(min = 3, max = 3) String currency,
            @jakarta.validation.constraints.DecimalMin("0.0") BigDecimal laborCost,
            @jakarta.validation.constraints.DecimalMin("0.0") BigDecimal partsCost,
            @jakarta.validation.constraints.Size(max = 150) String partName,
            @jakarta.validation.constraints.Size(max = 80) String partReference,
            @jakarta.validation.constraints.Size(max = 500) String invoiceUrl,
            LocalDate nextDueDate,
            @jakarta.validation.constraints.Min(0) Integer nextDueMileage
    ) {
    }

    public record MaintenanceRecordResponse(
            Long id,
            Long vehicleId,
            RecordType type,
            LocalDate serviceDate,
            Integer mileage,
            String description,
            Long providerId,
            String providerName,
            Long requestId,
            BigDecimal cost,
            String currency,
            BigDecimal laborCost,
            BigDecimal partsCost,
            String partName,
            String partReference,
            String invoiceUrl,
            LocalDate nextDueDate,
            Integer nextDueMileage,
            LocalDateTime createdAt
    ) {
    }
}
