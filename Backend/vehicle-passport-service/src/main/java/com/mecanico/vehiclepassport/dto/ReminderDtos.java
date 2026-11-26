package com.mecanico.vehiclepassport.dto;

import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ReminderType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReminderDtos {

    private ReminderDtos() {
    }

    public record ServiceReminderRequest(
            @jakarta.validation.constraints.NotNull ReminderType type,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 150) String title,
            @jakarta.validation.constraints.Min(0) Integer mileageTrigger,
            @jakarta.validation.constraints.Min(0) Integer nextDueMileage,
            LocalDate expiryDate,
            @jakarta.validation.constraints.Size(max = 2000) String notes
    ) {
    }

    public record ServiceReminderResponse(
            Long id,
            Long vehicleId,
            ReminderType type,
            String title,
            Integer mileageTrigger,
            Integer nextDueMileage,
            LocalDate expiryDate,
            ReminderStatus status,
            ReminderStatus storedStatus,
            boolean notified,
            LocalDateTime lastNotifiedAt,
            String notes,
            LocalDateTime completedAt,
            LocalDateTime createdAt,
            Integer kmUntilDue,
            Long daysUntilExpiry,
            boolean due,
            boolean overdue
    ) {
    }

    public record StatusUpdateRequest(
            @jakarta.validation.constraints.NotNull ReminderStatus status
    ) {
    }
}
