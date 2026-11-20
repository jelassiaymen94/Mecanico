package com.mecanico.vehiclepassport.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record AlertResponse(
            String type,
            Long vehicleId,
            String vehicleLabel,
            String title,
            String message,
            String severity,
            LocalDate dueDate,
            Integer mileage
    ) {
    }

    public record DashboardSummaryResponse(
            long totalVehicles,
            long activeVehicles,
            long documentsExpiring,
            long documentsExpired,
            long remindersDue,
            long remindersOverdue,
            BigDecimal totalMaintenanceCost,
            int averageHealthScore,
            List<VehicleDtos.VehicleResponse> vehicles,
            List<AlertResponse> alerts
    ) {
    }

    public record ScanResultResponse(
            int documentsNotified,
            int remindersNotified,
            LocalDateTime scannedAt
    ) {
    }

    public record NotificationResponse(
            Long id,
            Long vehicleId,
            Long documentId,
            Long reminderId,
            String eventType,
            String message,
            String channel,
            String status,
            LocalDateTime createdAt
    ) {
    }
}
