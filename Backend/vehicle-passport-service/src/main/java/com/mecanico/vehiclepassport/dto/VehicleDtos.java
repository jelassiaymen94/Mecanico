package com.mecanico.vehiclepassport.dto;

import com.mecanico.vehiclepassport.entity.FuelType;
import com.mecanico.vehiclepassport.entity.TransmissionType;
import com.mecanico.vehiclepassport.entity.VehicleStatus;
import com.mecanico.vehiclepassport.entity.VehicleType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class VehicleDtos {

    private VehicleDtos() {
    }

    public record VehicleRequest(
            Long ownerId,
            @jakarta.validation.constraints.NotNull Long brandId,
            @jakarta.validation.constraints.NotNull Long modelId,
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Size(min = 11, max = 17)
            @jakarta.validation.constraints.Pattern(regexp = "^[A-Za-z0-9]+$", message = "VIN must contain letters and digits only")
            String vin,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 20) String plate,
            @jakarta.validation.constraints.Size(max = 3) String plateCountry,
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Min(1950) @jakarta.validation.constraints.Max(2100) Integer year,
            @jakarta.validation.constraints.NotNull FuelType fuelType,
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Min(0) Integer mileage,
            @jakarta.validation.constraints.PastOrPresent LocalDate registrationDate,
            @jakarta.validation.constraints.Size(max = 40) String color,
            @jakarta.validation.constraints.Size(max = 80) String nickname,
            VehicleType vehicleType,
            TransmissionType transmission,
            @jakarta.validation.constraints.Min(50) @jakarta.validation.constraints.Max(15000) Integer engineCapacityCc,
            @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(2000) Integer powerHp,
            @jakarta.validation.constraints.Size(max = 500) String photoUrl,
            @jakarta.validation.constraints.Size(max = 2000) String notes,
            VehicleStatus status
    ) {
    }

    public record MileageRequest(
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Min(0) Integer mileage
    ) {
    }

    public record VehicleResponse(
            Long id,
            Long ownerId,
            Long brandId,
            String brandName,
            Long modelId,
            String modelName,
            String vin,
            String plate,
            String plateCountry,
            Integer year,
            FuelType fuelType,
            Integer mileage,
            LocalDate registrationDate,
            String color,
            String nickname,
            VehicleType vehicleType,
            TransmissionType transmission,
            Integer engineCapacityCc,
            Integer powerHp,
            String photoUrl,
            String notes,
            VehicleStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int healthScore,
            String healthGrade
    ) {
    }

    public record PassportStats(
            long recordCount,
            java.math.BigDecimal totalMaintenanceCost,
            int documentsTotal,
            int documentsExpiring,
            int documentsExpired,
            int remindersDue,
            int remindersOverdue,
            LocalDate lastServiceDate,
            int currentMileage
    ) {
    }

    public record PassportResponse(
            VehicleResponse vehicle,
            java.util.List<MaintenanceDtos.MaintenanceRecordResponse> records,
            java.util.List<DocumentDtos.VehicleDocumentResponse> documents,
            java.util.List<ReminderDtos.ServiceReminderResponse> reminders,
            HealthDtos.HealthScoreResponse health,
            PassportStats stats
    ) {
    }
}
