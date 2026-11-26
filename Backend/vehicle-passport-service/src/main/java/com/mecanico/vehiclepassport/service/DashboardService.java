package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.DashboardDtos;
import com.mecanico.vehiclepassport.dto.VehicleDtos;
import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.entity.VehicleDocument;
import com.mecanico.vehiclepassport.entity.VehicleStatus;
import com.mecanico.vehiclepassport.repository.MaintenanceRecordRepository;
import com.mecanico.vehiclepassport.repository.NotificationLogRepository;
import com.mecanico.vehiclepassport.repository.ServiceReminderRepository;
import com.mecanico.vehiclepassport.repository.VehicleDocumentRepository;
import com.mecanico.vehiclepassport.repository.VehicleRepository;
import com.mecanico.vehiclepassport.security.AuthUser;
import com.mecanico.vehiclepassport.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class DashboardService {

    private final VehicleRepository vehicles;
    private final VehicleDocumentRepository documents;
    private final ServiceReminderRepository reminders;
    private final MaintenanceRecordRepository records;
    private final NotificationLogRepository notifications;
    private final VehicleService vehicleService;
    private final DtoMapper mapper;
    private final CurrentUser currentUser;

    public DashboardService(VehicleRepository vehicles,
                            VehicleDocumentRepository documents,
                            ServiceReminderRepository reminders,
                            MaintenanceRecordRepository records,
                            NotificationLogRepository notifications,
                            VehicleService vehicleService,
                            DtoMapper mapper,
                            CurrentUser currentUser) {
        this.vehicles = vehicles;
        this.documents = documents;
        this.reminders = reminders;
        this.records = records;
        this.notifications = notifications;
        this.vehicleService = vehicleService;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public DashboardDtos.DashboardSummaryResponse summary() {
        AuthUser user = currentUser.require();
        boolean manager = "ADMIN".equals(user.role()) || "FLEET_MANAGER".equals(user.role());
        List<Vehicle> vehicleList = manager
                ? vehicles.findAllDetailed()
                : vehicles.findAllByOwnerIdOrderByCreatedAtDesc(user.id());

        LocalDate today = LocalDate.now();
        List<VehicleDtos.VehicleResponse> vehicleResponses = new ArrayList<>();
        List<DashboardDtos.AlertResponse> alerts = new ArrayList<>();
        long active = 0;
        long expiring = 0;
        long expired = 0;
        long due = 0;
        long overdue = 0;
        int scoreSum = 0;

        for (Vehicle vehicle : vehicleList) {
            VehicleDtos.VehicleResponse response = mapper.toVehicleResponse(vehicle);
            vehicleResponses.add(response);
            scoreSum += response.healthScore();
            if (vehicle.getStatus() == VehicleStatus.ACTIVE) {
                active++;
            }
            String label = vehicle.label();
            for (VehicleDocument document : documents.findAllByVehicleIdOrderByExpiryDateAsc(vehicle.getId())) {
                if (!document.isActive()) {
                    continue;
                }
                long days = ChronoUnit.DAYS.between(today, document.getExpiryDate());
                if (days < 0) {
                    expired++;
                    alerts.add(new DashboardDtos.AlertResponse(
                            "DOCUMENT", vehicle.getId(), label,
                            document.getType().name() + " expired",
                            document.getType() + " expired " + Math.abs(days) + " day(s) ago.",
                            "CRITICAL", document.getExpiryDate(), null));
                } else if (days <= 30) {
                    expiring++;
                    alerts.add(new DashboardDtos.AlertResponse(
                            "DOCUMENT", vehicle.getId(), label,
                            document.getType().name() + " expires soon",
                            document.getType() + " expires in " + days + " day(s).",
                            days <= 7 ? "WARNING" : "INFO", document.getExpiryDate(), null));
                }
            }
            for (ServiceReminder reminder : reminders.findAllByVehicleIdOrderByStatusAscNextDueMileageAsc(vehicle.getId())) {
                ReminderStatus status = ReminderEvaluator.effectiveStatus(reminder, vehicle, today);
                if (status == ReminderStatus.OVERDUE) {
                    overdue++;
                    alerts.add(new DashboardDtos.AlertResponse(
                            "REMINDER", vehicle.getId(), label,
                            reminder.getTitle(),
                            "Reminder is overdue. " + reminderMessage(reminder, vehicle),
                            "CRITICAL", reminder.getExpiryDate(), reminder.getNextDueMileage()));
                } else if (status == ReminderStatus.DUE) {
                    due++;
                    alerts.add(new DashboardDtos.AlertResponse(
                            "REMINDER", vehicle.getId(), label,
                            reminder.getTitle(),
                            "Reminder is due soon. " + reminderMessage(reminder, vehicle),
                            "WARNING", reminder.getExpiryDate(), reminder.getNextDueMileage()));
                }
            }
        }

        alerts.sort(Comparator
                .comparingInt((DashboardDtos.AlertResponse a) -> severityRank(a.severity()))
                .thenComparing(DashboardDtos.AlertResponse::dueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        if (alerts.size() > 25) {
            alerts = new ArrayList<>(alerts.subList(0, 25));
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        List<Long> ids = vehicleList.stream().map(Vehicle::getId).toList();
        if (!ids.isEmpty()) {
            BigDecimal sum = records.totalCostByVehicleIds(ids);
            totalCost = sum == null ? BigDecimal.ZERO : sum;
        }
        int averageScore = vehicleResponses.isEmpty() ? 0
                : (int) Math.round(scoreSum / (double) vehicleResponses.size());

        return new DashboardDtos.DashboardSummaryResponse(
                vehicleList.size(),
                active,
                expiring,
                expired,
                due,
                overdue,
                totalCost.setScale(2, RoundingMode.HALF_UP),
                averageScore,
                vehicleResponses,
                alerts
        );
    }

    @Transactional(readOnly = true)
    public List<DashboardDtos.NotificationResponse> recentNotifications() {
        Set<Long> accessible = vehicleService.accessibleVehicleIds();
        return notifications.findTop50ByOrderByCreatedAtDesc().stream()
                .filter(log -> log.getVehicleId() == null || accessible.contains(log.getVehicleId()))
                .map(mapper::toNotificationResponse)
                .toList();
    }

    private String reminderMessage(ServiceReminder reminder, Vehicle vehicle) {
        if (reminder.getNextDueMileage() != null) {
            return String.format("Due at %,.0f km (current: %,.0f km).",
                    reminder.getNextDueMileage().doubleValue(), vehicle.getMileage().doubleValue());
        }
        if (reminder.getExpiryDate() != null) {
            return "Due on " + reminder.getExpiryDate() + ".";
        }
        return "";
    }

    private int severityRank(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 0;
            case "WARNING" -> 1;
            default -> 2;
        };
    }
}
