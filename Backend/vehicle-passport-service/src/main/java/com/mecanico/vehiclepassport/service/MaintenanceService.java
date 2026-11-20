package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.MaintenanceDtos;
import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.exception.ApiException;
import com.mecanico.vehiclepassport.repository.MaintenanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaintenanceService {

    private final MaintenanceRecordRepository records;
    private final VehicleAccessService access;
    private final ReminderService reminderService;
    private final DtoMapper mapper;

    public MaintenanceService(MaintenanceRecordRepository records,
                              VehicleAccessService access,
                              ReminderService reminderService,
                              DtoMapper mapper) {
        this.records = records;
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
