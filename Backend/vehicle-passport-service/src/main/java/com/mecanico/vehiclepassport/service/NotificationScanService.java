package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.DashboardDtos;
import com.mecanico.vehiclepassport.entity.NotificationChannel;
import com.mecanico.vehiclepassport.entity.NotificationEventType;
import com.mecanico.vehiclepassport.entity.NotificationLog;
import com.mecanico.vehiclepassport.entity.NotificationStatus;
import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.entity.VehicleDocument;
import com.mecanico.vehiclepassport.repository.NotificationLogRepository;
import com.mecanico.vehiclepassport.repository.ServiceReminderRepository;
import com.mecanico.vehiclepassport.repository.VehicleDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Daily cron: detects documents expiring in 30 / 7 days and service reminders
 * that are due or overdue. In this first version events are persisted as
 * in-app notifications; publishing them to RabbitMQ is the next integration step.
 */
@Service
public class NotificationScanService {

    private static final Logger log = LoggerFactory.getLogger(NotificationScanService.class);

    private final VehicleDocumentRepository documents;
    private final ServiceReminderRepository reminders;
    private final NotificationLogRepository notifications;

    @Value("${mecanico.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public NotificationScanService(VehicleDocumentRepository documents,
                                   ServiceReminderRepository reminders,
                                   NotificationLogRepository notifications) {
        this.documents = documents;
        this.reminders = reminders;
        this.notifications = notifications;
    }

    @Scheduled(cron = "${mecanico.scheduler.document-scan-cron:0 0 8 * * *}")
    public void scheduledScan() {
        if (!schedulerEnabled) {
            return;
        }
        DashboardDtos.ScanResultResponse result = scanNow();
        log.info("Scheduled Mecanico scan finished: {} document(s), {} reminder(s) notified.",
                result.documentsNotified(), result.remindersNotified());
    }

    @Transactional
    public DashboardDtos.ScanResultResponse scanNow() {
        LocalDate today = LocalDate.now();
        int documentCount = 0;
        int reminderCount = 0;

        List<VehicleDocument> expiringDocuments =
                documents.findAllByExpiryDateBetweenAndActiveTrue(today, today.plusDays(30));
        for (VehicleDocument document : expiringDocuments) {
            long days = ChronoUnit.DAYS.between(today, document.getExpiryDate());
            if (days <= 7 && !document.isNotification7Sent()) {
                document.setNotification7Sent(true);
                document.setNotification30Sent(true);
                documentCount++;
                createDocumentNotification(document, NotificationEventType.DOCUMENT_EXPIRING_7, days);
                documents.save(document);
            } else if (days > 7 && !document.isNotification30Sent()) {
                document.setNotification30Sent(true);
                documentCount++;
                createDocumentNotification(document, NotificationEventType.DOCUMENT_EXPIRING_30, days);
                documents.save(document);
            }
        }

        List<ServiceReminder> openReminders =
                reminders.findAllByStatusIn(List.of(ReminderStatus.PENDING));
        for (ServiceReminder reminder : openReminders) {
            Vehicle vehicle = reminder.getVehicle();
            ReminderStatus effective = ReminderEvaluator.effectiveStatus(reminder, vehicle, today);
            if (effective != ReminderStatus.DUE && effective != ReminderStatus.OVERDUE) {
                continue;
            }
            NotificationEventType eventType = effective == ReminderStatus.OVERDUE
                    ? NotificationEventType.REMINDER_OVERDUE
                    : NotificationEventType.REMINDER_DUE;
            if (!notifications.existsByReminderIdAndEventType(reminder.getId(), eventType)) {
                createReminderNotification(reminder, vehicle, eventType);
                reminderCount++;
            }
            reminder.setNotified(true);
            reminder.setLastNotifiedAt(LocalDateTime.now());
            reminders.save(reminder);
        }

        log.info("Mecanico scan: {} document(s), {} reminder(s) notified.", documentCount, reminderCount);
        return new DashboardDtos.ScanResultResponse(documentCount, reminderCount, LocalDateTime.now());
    }

    private void createDocumentNotification(VehicleDocument document, NotificationEventType eventType, long days) {
        NotificationLog entry = new NotificationLog();
        entry.setVehicleId(document.getVehicle().getId());
        entry.setDocumentId(document.getId());
        entry.setEventType(eventType);
        entry.setMessage(String.format("%s %s expires in %d day(s).",
                document.getType(),
                document.getNumber() == null ? "" : "(" + document.getNumber() + ")",
                days));
        entry.setChannel(NotificationChannel.IN_APP);
        entry.setStatus(NotificationStatus.SENT);
        notifications.save(entry);
    }

    private void createReminderNotification(ServiceReminder reminder, Vehicle vehicle,
                                            NotificationEventType eventType) {
        NotificationLog entry = new NotificationLog();
        entry.setVehicleId(vehicle.getId());
        entry.setReminderId(reminder.getId());
        entry.setEventType(eventType);
        String suffix = reminder.getNextDueMileage() != null
                ? " (due at " + reminder.getNextDueMileage() + " km)"
                : reminder.getExpiryDate() != null ? " (due on " + reminder.getExpiryDate() + ")" : "";
        entry.setMessage(reminder.getTitle() + suffix + " - vehicle: " + vehicle.getPlate());
        entry.setChannel(NotificationChannel.IN_APP);
        entry.setStatus(NotificationStatus.SENT);
        notifications.save(entry);
    }
}
