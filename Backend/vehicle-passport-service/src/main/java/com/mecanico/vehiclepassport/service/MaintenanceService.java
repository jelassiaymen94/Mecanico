package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.MaintenanceDtos;
import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import com.mecanico.vehiclepassport.entity.RecordType;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.exception.ApiException;
import com.mecanico.vehiclepassport.messaging.Events;
import com.mecanico.vehiclepassport.repository.MaintenanceRecordRepository;
import com.mecanico.vehiclepassport.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);

    private final MaintenanceRecordRepository records;
    private final VehicleRepository vehicles;
    private final VehicleAccessService access;
    private final ReminderService reminderService;
    private final DtoMapper mapper;

    public MaintenanceService(MaintenanceRecordRepository records,
                              VehicleRepository vehicles,
                              VehicleAccessService access,
                              ReminderService reminderService,
                              DtoMapper mapper) {
        this.records = records;
        this.vehicles = vehicles;
        this.access = access;
        this.reminderService = reminderService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDtos.MaintenanceRecordResponse> listForVehicle(Long vehicleId) {
        access.requireReadable(vehicleId);
        return records.findAllByVehicleIdOrderByServiceDateDescMileageDesc(vehicleId)
                .stream().map(mapper::toRecordResponse).toList();
    }

    @Transactional
    public MaintenanceDtos.MaintenanceRecordResponse add(Long vehicleId, MaintenanceDtos.MaintenanceRecordRequest request) {
        Vehicle vehicle = access.requireWritable(vehicleId);
        MaintenanceRecord record = new MaintenanceRecord();
        record.setVehicle(vehicle);
        record.setType(request.type());
        record.setServiceDate(request.serviceDate());
        record.setMileage(request.mileage());
        record.setDescription(trimToNull(request.description()));
        record.setProviderId(request.providerId());
        record.setProviderName(trimToNull(request.providerName()));
        record.setRequestId(request.requestId());
        record.setCost(request.cost());
        record.setCurrency(request.currency() == null || request.currency().isBlank()
                ? "TND" : request.currency().trim().toUpperCase());
        record.setLaborCost(request.laborCost());
        record.setPartsCost(request.partsCost());
        record.setPartName(trimToNull(request.partName()));
        record.setPartReference(trimToNull(request.partReference()));
        record.setInvoiceUrl(trimToNull(request.invoiceUrl()));
        record.setNextDueDate(request.nextDueDate());
        record.setNextDueMileage(request.nextDueMileage());
        MaintenanceRecord saved = records.save(record);

        if (safe(record.getMileage()) > safe(vehicle.getMileage())) {
            vehicle.setMileage(record.getMileage());
        }
        reminderService.afterMaintenanceRecord(vehicle, saved);
        return mapper.toRecordResponse(saved);
    }

    @Transactional
    public void delete(Long vehicleId, Long recordId) {
        access.requireWritable(vehicleId);
        MaintenanceRecord record = records.findByIdAndVehicleId(recordId, vehicleId)
                .orElseThrow(() -> ApiException.notFound("Maintenance record not found"));
        records.delete(record);
    }

    /**
     * Async entry point (RabbitMQ RequestCompleted from MS2): appends the completed
     * intervention to the vehicle passport. Idempotent on {@code requestId}, no user context.
     */
    @Transactional
    public void recordFromCompletedRequest(Events.RequestCompletedEvent event) {
        if (event.vehicleId() == null || event.requestId() == null) {
            log.info("RequestCompleted {} ignored: no vehicle attached", event.requestId());
            return;
        }
        if (records.existsByRequestId(event.requestId())) {
            log.info("RequestCompleted {} already recorded, skipping", event.requestId());
            return;
        }
        Vehicle vehicle = vehicles.findById(event.vehicleId()).orElse(null);
        if (vehicle == null) {
            log.warn("RequestCompleted {} references unknown vehicle {}", event.requestId(), event.vehicleId());
            return;
        }
        MaintenanceRecord record = new MaintenanceRecord();
        record.setVehicle(vehicle);
        record.setType(recordTypeFor(event.category()));
        record.setServiceDate(event.completedAt() != null ? event.completedAt().toLocalDate() : LocalDate.now());
        record.setMileage(Math.max(safe(event.mileage()), safe(vehicle.getMileage())));
        String title = trimToNull(event.title());
        String description = trimToNull(event.description());
        record.setDescription(title == null ? description
                : description == null ? title : title + " - " + description);
        record.setProviderId(event.providerId());
        record.setRequestId(event.requestId());
        record.setCost(event.finalPrice());
        record.setCurrency("TND");
        MaintenanceRecord saved = records.save(record);
        if (safe(record.getMileage()) > safe(vehicle.getMileage())) {
            vehicle.setMileage(record.getMileage());
        }
        reminderService.afterMaintenanceRecord(vehicle, saved);
        log.info("Maintenance record {} created from request {}", saved.getId(), event.requestId());
    }

    private RecordType recordTypeFor(String category) {
        if (category == null) {
            return RecordType.SERVICE;
        }
        return switch (category.toUpperCase()) {
            case "MECHANIC", "TOWING", "TOW" -> RecordType.REPAIR;
            case "INSPECTION" -> RecordType.INSPECTION;
            default -> RecordType.SERVICE;
        };
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
