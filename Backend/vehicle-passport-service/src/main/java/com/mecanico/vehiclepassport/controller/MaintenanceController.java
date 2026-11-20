package com.mecanico.vehiclepassport.controller;

import com.mecanico.vehiclepassport.dto.MaintenanceDtos;
import com.mecanico.vehiclepassport.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/records")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public List<MaintenanceDtos.MaintenanceRecordResponse> list(@PathVariable Long vehicleId) {
        return maintenanceService.listForVehicle(vehicleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceDtos.MaintenanceRecordResponse add(
            @PathVariable Long vehicleId,
            @Valid @RequestBody MaintenanceDtos.MaintenanceRecordRequest request) {
        return maintenanceService.add(vehicleId, request);
    }

    @DeleteMapping("/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long vehicleId, @PathVariable Long recordId) {
        maintenanceService.delete(vehicleId, recordId);
    }
}
