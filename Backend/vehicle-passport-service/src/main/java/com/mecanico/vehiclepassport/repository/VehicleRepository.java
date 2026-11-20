package com.mecanico.vehiclepassport.repository;

import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.entity.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    @Override
    @EntityGraph(attributePaths = {"brand", "model"})
    Optional<Vehicle> findById(Long id);

    @EntityGraph(attributePaths = {"brand", "model"})
    Page<Vehicle> findByOwnerId(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "model"})
    List<Vehicle> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"brand", "model"})
    List<Vehicle> findAllByOwnerIdInOrderByCreatedAtDesc(List<Long> ownerIds);

    @EntityGraph(attributePaths = {"brand", "model"})
    @Query("select v from Vehicle v order by v.createdAt desc")
    List<Vehicle> findAllDetailed();

    long countByOwnerId(Long ownerId);

    long countByStatus(VehicleStatus status);

    @Query("select count(v) from Vehicle v where v.ownerId = :ownerId and v.status = :status")
    long countByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") VehicleStatus status);

    boolean existsByVinIgnoreCase(String vin);

    boolean existsByPlateIgnoreCase(String plate);

    boolean existsByVinIgnoreCaseAndIdNot(String vin, Long id);

    boolean existsByPlateIgnoreCaseAndIdNot(String plate, Long id);
}
