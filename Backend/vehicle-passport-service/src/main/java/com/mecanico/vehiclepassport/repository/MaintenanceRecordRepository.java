package com.mecanico.vehiclepassport.repository;

import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    List<MaintenanceRecord> findAllByVehicleIdOrderByServiceDateDescMileageDesc(Long vehicleId);

    Optional<MaintenanceRecord> findByIdAndVehicleId(Long id, Long vehicleId);

    Optional<MaintenanceRecord> findFirstByVehicleIdOrderByServiceDateDescMileageDesc(Long vehicleId);

    long countByVehicleId(Long vehicleId);

    boolean existsByRequestId(Long requestId);

    @Query("select coalesce(sum(m.cost), 0) from MaintenanceRecord m where m.vehicle.id = :vehicleId")
    BigDecimal totalCostByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("select coalesce(sum(m.cost), 0) from MaintenanceRecord m where m.vehicle.id in :vehicleIds")
    BigDecimal totalCostByVehicleIds(@Param("vehicleIds") Collection<Long> vehicleIds);

    void deleteByVehicleId(Long vehicleId);
}
