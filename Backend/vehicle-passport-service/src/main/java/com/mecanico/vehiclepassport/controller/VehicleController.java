package com.mecanico.vehiclepassport.controller;

import com.mecanico.vehiclepassport.dto.CommonDtos;
import com.mecanico.vehiclepassport.dto.HealthDtos;
import com.mecanico.vehiclepassport.dto.VehicleDtos;
import com.mecanico.vehiclepassport.entity.VehicleStatus;
import com.mecanico.vehiclepassport.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDtos.VehicleResponse create(@Valid @RequestBody VehicleDtos.VehicleRequest request) {
        return vehicleService.create(request);
    }

    @GetMapping
    public CommonDtos.PageResponse<VehicleDtos.VehicleResponse> list(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return vehicleService.search(ownerId, search, status, page, size);
    }

    @GetMapping("/{id}")
    public VehicleDtos.VehicleResponse get(@PathVariable Long id) {
        return vehicleService.get(id);
    }

    @PutMapping("/{id}")
    public VehicleDtos.VehicleResponse update(@PathVariable Long id,
                                              @Valid @RequestBody VehicleDtos.VehicleRequest request) {
        return vehicleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vehicleService.delete(id);
    }

    @PatchMapping("/{id}/mileage")
    public VehicleDtos.VehicleResponse updateMileage(@PathVariable Long id,
                                                      @Valid @RequestBody VehicleDtos.MileageRequest request) {
        return vehicleService.updateMileage(id, request.mileage());
    }

    @GetMapping("/{id}/passport")
    public VehicleDtos.PassportResponse passport(@PathVariable Long id) {
        return vehicleService.passport(id);
    }

    @GetMapping("/{id}/health")
    public HealthDtos.HealthScoreResponse health(@PathVariable Long id) {
        return vehicleService.health(id);
    }
}
