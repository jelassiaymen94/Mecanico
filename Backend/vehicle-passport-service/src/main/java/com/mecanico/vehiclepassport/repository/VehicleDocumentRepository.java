package com.mecanico.vehiclepassport.repository;

import com.mecanico.vehiclepassport.entity.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Long> {

    List<VehicleDocument> findAllByVehicleIdOrderByExpiryDateAsc(Long vehicleId);

    Optional<VehicleDocument> findByIdAndVehicleId(Long id, Long vehicleId);

    List<VehicleDocument> findAllByExpiryDateBetweenAndActiveTrue(LocalDate from, LocalDate to);

    List<VehicleDocument> findAllByExpiryDateBeforeAndActiveTrue(LocalDate date);

    long countByVehicleIdAndExpiryDateBeforeAndActiveTrue(Long vehicleId, LocalDate date);

    long countByVehicleIdAndExpiryDateBetweenAndActiveTrue(Long vehicleId, LocalDate from, LocalDate to);

    void deleteByVehicleId(Long vehicleId);
}
