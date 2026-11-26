package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.ReminderDtos;
import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import com.mecanico.vehiclepassport.entity.RecordType;
import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ReminderType;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.exception.ApiException;
import com.mecanico.vehiclepassport.repository.ServiceReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    private static final List<ReminderStatus> OPEN_STATUSES =
            List.of(ReminderStatus.PENDING, ReminderStatus.DUE, ReminderStatus.OVERDUE);

    private final ServiceReminderRepository reminders;
    private final VehicleAccessService access;
    private final DtoMapper mapper;

    public ReminderService(ServiceReminderRepository reminders, VehicleAccessService access, DtoMapper mapper) {
        this.reminders = reminders;
        this.access = access;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ReminderDtos.ServiceReminderResponse> listForVehicle(Long vehicleId) {
        access.requireReadable(vehicleId);
        return reminders.findAllByVehicleIdOrderByStatusAscNextDueMileageAsc(vehicleId)
                .stream().map(mapper::toReminderResponse).toList();
    }

    @Transactional
    public ReminderDtos.ServiceReminderResponse add(Long vehicleId, ReminderDtos.ServiceReminderRequest request) {
        Vehicle vehicle = access.requireWritable(vehicleId);
        if (request.nextDueMileage() == null && request.expiryDate() == null) {
            throw ApiException.badRequest("A reminder needs a mileage trigger or an expiry date");
        }
        ServiceReminder reminder = new ServiceReminder();
        reminder.setVehicle(vehicle);
        reminder.setType(request.type() == null ? ReminderType.CUSTOM : request.type());
        reminder.setTitle(request.title().trim());
        reminder.setMileageTrigger(request.mileageTrigger());
        reminder.setNextDueMileage(request.nextDueMileage());
        reminder.setExpiryDate(request.expiryDate());
        reminder.setNotes(request.notes());
        reminder.setStatus(ReminderStatus.PENDING);
        return mapper.toReminderResponse(reminders.save(reminder));
    }

    @Transactional
    public ReminderDtos.ServiceReminderResponse updateStatus(Long vehicleId, Long reminderId,
                                                              ReminderDtos.StatusUpdateRequest request) {
        access.requireWritable(vehicleId);
        ServiceReminder reminder = reminders.findByIdAndVehicleId(reminderId, vehicleId)
                .orElseThrow(() -> ApiException.notFound("Reminder not found"));
        if (request.status() != ReminderStatus.PENDING
                && request.status() != ReminderStatus.COMPLETED
                && request.status() != ReminderStatus.DISMISSED) {
            throw ApiException.badRequest("Reminder status can only be set to PENDING, COMPLETED or DISMISSED");
        }
        reminder.setStatus(request.status());
        if (request.status() == ReminderStatus.COMPLETED) {
            reminder.setCompletedAt(LocalDateTime.now());
        } else {
            reminder.setCompletedAt(null);
            reminder.setNotified(false);
            reminder.setLastNotifiedAt(null);
        }
        return mapper.toReminderResponse(reminders.save(reminder));
    }

    @Transactional
    public void delete(Long vehicleId, Long reminderId) {
        access.requireWritable(vehicleId);
        ServiceReminder reminder = reminders.findByIdAndVehicleId(reminderId, vehicleId)
                .orElseThrow(() -> ApiException.notFound("Reminder not found"));
        reminders.delete(reminder);
    }

    @Transactional
    public void createDefaultReminders(Vehicle vehicle) {
        createReminder(vehicle, ReminderType.OIL_CHANGE,
                "Oil change - every 10,000 km",
                10_000, safeMileage(vehicle) + 10_000, null,
                "Automatically created from the 10,000 km maintenance rule.");
        createReminder(vehicle, ReminderType.TIRE_ROTATION,
                "Tire rotation and pressure check",
                10_000, safeMileage(vehicle) + 10_000, null,
                "Automatically created default reminder.");
    }

    /**
     * Called when a maintenance record is added. A SERVICE entry closes the open oil-change
     * reminder and schedules the next one 10,000 km later (or at the requested mileage).
     */
    @Transactional
    public void afterMaintenanceRecord(Vehicle vehicle, MaintenanceRecord record) {
        if (record.getType() != RecordType.SERVICE) {
            return;
        }
        reminders.findFirstByVehicleIdAndTypeAndStatusInOrderByCreatedAtDesc(
                        vehicle.getId(), ReminderType.OIL_CHANGE, OPEN_STATUSES)
                .ifPresent(existing -> {
                    existing.setStatus(ReminderStatus.COMPLETED);
                    existing.setCompletedAt(LocalDateTime.now());
                    reminders.save(existing);
                });
        int nextDue = record.getNextDueMileage() != null
                ? record.getNextDueMileage()
                : Math.max(record.getMileage() + 10_000, safeMileage(vehicle) + 10_000);
        createReminder(vehicle, ReminderType.OIL_CHANGE,
                "Oil change - every 10,000 km",
                10_000, nextDue, record.getNextDueDate(),
                "Next oil change scheduled after the " + record.getServiceDate() + " service.");
    }

    private void createReminder(Vehicle vehicle, ReminderType type, String title, Integer trigger,
                                Integer nextDueMileage, java.time.LocalDate expiryDate, String notes) {
        ServiceReminder reminder = new ServiceReminder();
        reminder.setVehicle(vehicle);
        reminder.setType(type);
        reminder.setTitle(title);
        reminder.setMileageTrigger(trigger);
        reminder.setNextDueMileage(nextDueMileage);
        reminder.setExpiryDate(expiryDate);
        reminder.setNotes(notes);
        reminders.save(reminder);
    }

    private int safeMileage(Vehicle vehicle) {
        return vehicle.getMileage() == null ? 0 : vehicle.getMileage();
    }
}
