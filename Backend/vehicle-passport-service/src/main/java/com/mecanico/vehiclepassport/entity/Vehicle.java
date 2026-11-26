package com.mecanico.vehiclepassport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehicle_vin", columnNames = "vin"),
        @UniqueConstraint(name = "uk_vehicle_plate", columnNames = "plate")
})
@Getter
@Setter
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private VehicleBrand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @Column(nullable = false, length = 17)
    private String vin;

    @Column(nullable = false, length = 20)
    private String plate;

    @Column(name = "plate_country", nullable = false, length = 3)
    private String plateCountry = "TN";

    @Column(name = "model_year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType = FuelType.PETROL;

    @Column(nullable = false)
    private Integer mileage = 0;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(length = 40)
    private String color;

    @Column(length = 80)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType = VehicleType.CAR;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransmissionType transmission;

    @Column(name = "engine_capacity_cc")
    private Integer engineCapacityCc;

    @Column(name = "power_hp")
    private Integer powerHp;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String label() {
        String brandName = brand != null ? brand.getName() : "";
        String modelName = model != null ? model.getName() : "";
        return (brandName + " " + modelName).trim() + " · " + plate;
    }
}
