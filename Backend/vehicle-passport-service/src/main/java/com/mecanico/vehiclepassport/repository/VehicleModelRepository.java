package com.mecanico.vehiclepassport.repository;

import com.mecanico.vehiclepassport.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    List<VehicleModel> findAllByBrandIdAndActiveTrueOrderByNameAsc(Long brandId);

    long countByBrandIdAndActiveTrue(Long brandId);

    Optional<VehicleModel> findByIdAndBrandIdAndActiveTrue(Long id, Long brandId);

    Optional<VehicleModel> findByBrandIdAndNameIgnoreCase(Long brandId, String name);

    @Query("""
            select m from VehicleModel m
            join fetch m.brand b
            where m.active = true
              and (:brandId is null or b.id = :brandId)
              and (:search is null or lower(m.name) like :search)
            order by b.name asc, m.name asc
            """)
    List<VehicleModel> search(@Param("brandId") Long brandId, @Param("search") String search);

    boolean existsByBrandIdAndNameIgnoreCase(Long brandId, String name);
}
