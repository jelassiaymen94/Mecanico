package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import com.mecanico.vehiclepassport.entity.Vehicle;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Deterministic reminder rules shared by API mapping, the health score and the cron scanner.
 */
public final class ReminderEvaluator {

    private ReminderEvaluator() {
    }

    public static ReminderStatus effectiveStatus(ServiceReminder reminder, Vehicle vehicle, LocalDate today) {
        if (reminder.getStatus() == ReminderStatus.COMPLETED || reminder.getStatus() == ReminderStatus.DISMISSED) {
            return reminder.getStatus();
        }
        int currentMileage = vehicle.getMileage() == null ? 0 : vehicle.getMileage();
        boolean expired = reminder.getExpiryDate() != null && reminder.getExpiryDate().isBefore(today);
        boolean mileageOverdue = reminder.getNextDueMileage() != null && currentMileage > reminder.getNextDueMileage();
        if (expired || mileageOverdue) {
            return ReminderStatus.OVERDUE;
        }
        boolean expiringSoon = reminder.getExpiryDate() != null
                && !reminder.getExpiryDate().isAfter(today.plusDays(30));
        boolean mileageSoon = reminder.getNextDueMileage() != null
                && reminder.getNextDueMileage() - currentMileage <= 500;
        if (expiringSoon || mileageSoon) {
            return ReminderStatus.DUE;
        }
        return ReminderStatus.PENDING;
    }

    public static boolean isOverdue(ServiceReminder reminder, Vehicle vehicle, LocalDate today) {
        return effectiveStatus(reminder, vehicle, today) == ReminderStatus.OVERDUE;
    }

    public static boolean isDue(ServiceReminder reminder, Vehicle vehicle, LocalDate today) {
        return effectiveStatus(reminder, vehicle, today) == ReminderStatus.DUE;
    }

    public static Integer kmUntilDue(ServiceReminder reminder, Vehicle vehicle) {
        if (reminder.getNextDueMileage() == null) {
            return null;
        }
        int current = vehicle.getMileage() == null ? 0 : vehicle.getMileage();
        return reminder.getNextDueMileage() - current;
    }

    public static Long daysUntilExpiry(ServiceReminder reminder, LocalDate today) {
        if (reminder.getExpiryDate() == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(today, reminder.getExpiryDate());
    }
}
