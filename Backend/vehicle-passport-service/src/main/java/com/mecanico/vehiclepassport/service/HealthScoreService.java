package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.HealthDtos;
import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.entity.VehicleDocument;
import com.mecanico.vehiclepassport.repository.MaintenanceRecordRepository;
import com.mecanico.vehiclepassport.repository.ServiceReminderRepository;
import com.mecanico.vehiclepassport.repository.VehicleDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic business rules - no AI. The score starts at 100 and points are deducted
 * for age, mileage, missing maintenance, expired documents and overdue reminders.
 */
@Service
public class HealthScoreService {

    private final MaintenanceRecordRepository records;
    private final VehicleDocumentRepository documents;
    private final ServiceReminderRepository reminders;

    public HealthScoreService(MaintenanceRecordRepository records,
                              VehicleDocumentRepository documents,
                              ServiceReminderRepository reminders) {
        this.records = records;
        this.documents = documents;
        this.reminders = reminders;
    }

    @Transactional(readOnly = true)
    public HealthDtos.HealthScoreResponse compute(Vehicle vehicle) {
        LocalDate today = LocalDate.now();
        List<MaintenanceRecord> history = records.findAllByVehicleIdOrderByServiceDateDescMileageDesc(vehicle.getId());
        List<VehicleDocument> docs = documents.findAllByVehicleIdOrderByExpiryDateAsc(vehicle.getId())
                .stream().filter(VehicleDocument::isActive).toList();
        List<ServiceReminder> reminderList = reminders.findAllByVehicleIdOrderByStatusAscNextDueMileageAsc(vehicle.getId());

        List<HealthDtos.HealthFactor> factors = new ArrayList<>();
        Map<String, Integer> breakdown = new LinkedHashMap<>();

        int ageYears = calculateAge(vehicle, today);
        int agePenalty = Math.min(20, Math.max(0, ageYears - 4) * 2);
        breakdown.put("age", agePenalty);
        factors.add(new HealthDtos.HealthFactor("AGE", "Vehicle age",
                agePenalty > 0 ? "WARNING" : "GOOD",
                ageYears + " year(s) old" + (agePenalty > 0 ? " - " + agePenalty + " point(s) deducted" : ""),
                agePenalty));

        int mileagePenalty = Math.min(20, (safe(vehicle.getMileage()) / 20_000) * 2);
        breakdown.put("mileage", mileagePenalty);
        factors.add(new HealthDtos.HealthFactor("MILEAGE", "Mileage",
                mileagePenalty > 0 ? "WARNING" : "GOOD",
                String.format("%,d km on the odometer", safe(vehicle.getMileage())),
                mileagePenalty));

        int maintenancePenalty = 0;
        String maintenanceDetail;
        String maintenanceStatus;
        if (history.isEmpty()) {
            maintenancePenalty = 15;
            maintenanceDetail = "No maintenance record in the passport";
            maintenanceStatus = "CRITICAL";
        } else {
            MaintenanceRecord latest = history.get(0);
            long monthsSince = ChronoUnit.MONTHS.between(latest.getServiceDate(), today);
            int kmSince = safe(vehicle.getMileage()) - safe(latest.getMileage());
            List<String> issues = new ArrayList<>();
            if (monthsSince > 12) {
                maintenancePenalty += 10;
                issues.add("last service was " + monthsSince + " months ago");
            }
            if (kmSince > 15_000) {
                maintenancePenalty += 10;
                issues.add(String.format("%,d km since the last service", kmSince));
            }
            maintenancePenalty = Math.min(25, maintenancePenalty);
            maintenanceDetail = issues.isEmpty()
                    ? history.size() + " record(s), last service " + latest.getServiceDate()
                    : String.join(", ", issues);
            maintenanceStatus = issues.isEmpty() ? "GOOD" : "WARNING";
        }
        breakdown.put("maintenance", maintenancePenalty);
        factors.add(new HealthDtos.HealthFactor("MAINTENANCE", "Maintenance history",
                maintenanceStatus, maintenanceDetail, maintenancePenalty));

        long expiredDocuments = docs.stream().filter(d -> d.getExpiryDate().isBefore(today)).count();
        long expiringDocuments = docs.stream()
                .filter(d -> !d.getExpiryDate().isBefore(today))
                .filter(d -> !d.getExpiryDate().isAfter(today.plusDays(30)))
                .count();
        int documentPenalty = Math.min(21, (int) expiredDocuments * 5 + (int) expiringDocuments * 2);
        breakdown.put("documents", documentPenalty);
        factors.add(new HealthDtos.HealthFactor("DOCUMENTS", "Administrative documents",
                expiredDocuments > 0 ? "CRITICAL" : expiringDocuments > 0 ? "WARNING" : "GOOD",
                expiredDocuments + " expired, " + expiringDocuments + " expiring within 30 days",
                documentPenalty));

        long overdueReminders = 0;
        long dueReminders = 0;
        for (ServiceReminder reminder : reminderList) {
            ReminderStatus status = ReminderEvaluator.effectiveStatus(reminder, vehicle, today);
            if (status == ReminderStatus.OVERDUE) {
                overdueReminders++;
            } else if (status == ReminderStatus.DUE) {
                dueReminders++;
            }
        }
        int reminderPenalty = Math.min(15, (int) overdueReminders * 5 + (int) dueReminders * 2);
        breakdown.put("reminders", reminderPenalty);
        factors.add(new HealthDtos.HealthFactor("REMINDERS", "Due reminders",
                overdueReminders > 0 ? "CRITICAL" : dueReminders > 0 ? "WARNING" : "GOOD",
                overdueReminders + " overdue, " + dueReminders + " due soon",
                reminderPenalty));

        int totalPenalty = breakdown.values().stream().mapToInt(Integer::intValue).sum();
        int score = Math.max(0, Math.min(100, 100 - totalPenalty));
        String grade = score >= 85 ? "EXCELLENT" : score >= 70 ? "GOOD" : score >= 50 ? "FAIR" : "POOR";
        String summary = switch (grade) {
            case "EXCELLENT" -> "Vehicle is in excellent condition. Keep up the regular maintenance.";
            case "GOOD" -> "Vehicle is in good condition with a few points to monitor.";
            case "FAIR" -> "Vehicle needs attention: review the warnings below.";
            default -> "Vehicle health is critical. Schedule maintenance and renew documents.";
        };

        return new HealthDtos.HealthScoreResponse(score, grade, summary, breakdown, factors);
    }

    private int calculateAge(Vehicle vehicle, LocalDate today) {
        if (vehicle.getRegistrationDate() != null) {
            return Math.max(0, Period.between(vehicle.getRegistrationDate(), today).getYears());
        }
        return Math.max(0, today.getYear() - safe(vehicle.getYear()));
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
