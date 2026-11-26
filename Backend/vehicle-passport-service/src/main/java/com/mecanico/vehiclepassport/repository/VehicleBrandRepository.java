package com.mecanico.vehiclepassport.repository;

import com.mecanico.vehiclepassport.entity.VehicleBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, Long> {

    List<VehicleBrand> findAllByActiveTrueOrderByNameAsc();

    Optional<VehicleBrand> findByIdAndActiveTrue(Long id);

    Optional<VehicleBrand> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
