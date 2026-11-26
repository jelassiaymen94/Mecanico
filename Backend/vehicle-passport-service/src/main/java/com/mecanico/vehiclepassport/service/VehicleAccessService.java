package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.exception.ApiException;
import com.mecanico.vehiclepassport.repository.VehicleRepository;
import com.mecanico.vehiclepassport.security.AuthUser;
import com.mecanico.vehiclepassport.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleAccessService {

    private final VehicleRepository vehicles;
    private final CurrentUser currentUser;

    public VehicleAccessService(VehicleRepository vehicles, CurrentUser currentUser) {
        this.vehicles = vehicles;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public Vehicle requireReadable(Long vehicleId) {
        Vehicle vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        AuthUser user = currentUser.require();
        if (isManager(user) || vehicle.getOwnerId().equals(user.id())) {
            return vehicle;
        }
        throw ApiException.forbidden("You do not have access to this vehicle");
    }

    @Transactional(readOnly = true)
    public Vehicle requireWritable(Long vehicleId) {
        Vehicle vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        AuthUser user = currentUser.require();
        if (isManager(user) || vehicle.getOwnerId().equals(user.id())) {
            return vehicle;
        }
        throw ApiException.forbidden("You do not have access to this vehicle");
    }

    public boolean canRead(Vehicle vehicle) {
        AuthUser user = currentUser.require();
        return isManager(user) || vehicle.getOwnerId().equals(user.id());
    }

    private boolean isManager(AuthUser user) {
        return "ADMIN".equals(user.role()) || "FLEET_MANAGER".equals(user.role());
    }
}
