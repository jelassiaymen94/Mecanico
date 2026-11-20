package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.DashboardDtos;
import com.mecanico.vehiclepassport.dto.DocumentDtos;
import com.mecanico.vehiclepassport.dto.HealthDtos;
import com.mecanico.vehiclepassport.dto.MaintenanceDtos;
import com.mecanico.vehiclepassport.dto.ReminderDtos;
import com.mecanico.vehiclepassport.dto.VehicleDtos;
import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import com.mecanico.vehiclepassport.entity.NotificationLog;
import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.entity.VehicleDocument;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class DtoMapper {

    private final HealthScoreService healthScoreService;

    public DtoMapper(HealthScoreService healthScoreService) {
        this.healthScoreService = healthScoreService;
    }

    public VehicleDtos.VehicleResponse toVehicleResponse(Vehicle vehicle) {
        HealthDtos.HealthScoreResponse health = healthScoreService.compute(vehicle);
        return new VehicleDtos.VehicleResponse(
                vehicle.getId(),
                vehicle.getOwnerId(),
                vehicle.getBrand().getId(),
                vehicle.getBrand().getName(),
                vehicle.getModel().getId(),
                vehicle.getModel().getName(),
                vehicle.getVin(),
                vehicle.getPlate(),
                vehicle.getPlateCountry(),
                vehicle.getYear(),
                vehicle.getFuelType(),
                vehicle.getMileage(),
                vehicle.getRegistrationDate(),
                vehicle.getColor(),
                vehicle.getNickname(),
                vehicle.getVehicleType(),
                vehicle.getTransmission(),
                vehicle.getEngineCapacityCc(),
                vehicle.getPowerHp(),
                vehicle.getPhotoUrl(),
                vehicle.getNotes(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt(),
                health.score(),
                health.grade()
        );
    }

    public MaintenanceDtos.MaintenanceRecordResponse toRecordResponse(MaintenanceRecord record) {
        return new MaintenanceDtos.MaintenanceRecordResponse(
                record.getId(),
                record.getVehicle().getId(),
                record.getType(),
                record.getServiceDate(),
                record.getMileage(),
                record.getDescription(),
                record.getProviderId(),
                record.getProviderName(),
                record.getRequestId(),
                record.getCost(),
                record.getCurrency(),
                record.getLaborCost(),
                record.getPartsCost(),
                record.getPartName(),
                record.getPartReference(),
                record.getInvoiceUrl(),
                record.getNextDueDate(),
                record.getNextDueMileage(),
                record.getCreatedAt()
        );
    }

    public DocumentDtos.VehicleDocumentResponse toDocumentResponse(VehicleDocument document) {
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, document.getExpiryDate());
        boolean expired = document.getExpiryDate().isBefore(today);
        boolean expiringSoon = !expired && days <= 30;
        return new DocumentDtos.VehicleDocumentResponse(
                document.getId(),
                document.getVehicle().getId(),
                document.getType(),
                document.getNumber(),
                document.getIssuer(),
                document.getIssueDate(),
                document.getExpiryDate(),
                document.getFileUrl(),
                document.getCost(),
                document.getNotes(),
                document.isNotification30Sent(),
                document.isNotification7Sent(),
                document.isActive(),
                days,
                expired,
                expiringSoon,
                document.getCreatedAt()
        );
    }

    public ReminderDtos.ServiceReminderResponse toReminderResponse(ServiceReminder reminder) {
        LocalDate today = LocalDate.now();
        Vehicle vehicle = reminder.getVehicle();
        ReminderStatus effective = ReminderEvaluator.effectiveStatus(reminder, vehicle, today);
        return new ReminderDtos.ServiceReminderResponse(
                reminder.getId(),
                vehicle.getId(),
                reminder.getType(),
                reminder.getTitle(),
                reminder.getMileageTrigger(),
                reminder.getNextDueMileage(),
                reminder.getExpiryDate(),
                effective,
                reminder.getStatus(),
                reminder.isNotified(),
                reminder.getLastNotifiedAt(),
                reminder.getNotes(),
                reminder.getCompletedAt(),
                reminder.getCreatedAt(),
                ReminderEvaluator.kmUntilDue(reminder, vehicle),
                ReminderEvaluator.daysUntilExpiry(reminder, today),
                effective == ReminderStatus.DUE,
                effective == ReminderStatus.OVERDUE
        );
    }

    public DashboardDtos.NotificationResponse toNotificationResponse(NotificationLog log) {
        return new DashboardDtos.NotificationResponse(
                log.getId(),
                log.getVehicleId(),
                log.getDocumentId(),
                log.getReminderId(),
                log.getEventType().name(),
                log.getMessage(),
                log.getChannel().name(),
                log.getStatus().name(),
                log.getCreatedAt()
        );
    }
}
