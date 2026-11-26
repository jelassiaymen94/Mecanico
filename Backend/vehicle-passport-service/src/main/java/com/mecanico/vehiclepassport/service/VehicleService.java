package com.mecanico.vehiclepassport.service;

import com.mecanico.vehiclepassport.dto.CommonDtos;
import com.mecanico.vehiclepassport.dto.HealthDtos;
import com.mecanico.vehiclepassport.dto.VehicleDtos;
import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import com.mecanico.vehiclepassport.entity.ReminderStatus;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.entity.VehicleBrand;
import com.mecanico.vehiclepassport.entity.VehicleDocument;
import com.mecanico.vehiclepassport.entity.VehicleModel;
import com.mecanico.vehiclepassport.entity.VehicleStatus;
import com.mecanico.vehiclepassport.exception.ApiException;
import com.mecanico.vehiclepassport.repository.MaintenanceRecordRepository;
import com.mecanico.vehiclepassport.repository.NotificationLogRepository;
import com.mecanico.vehiclepassport.repository.ServiceReminderRepository;
import com.mecanico.vehiclepassport.repository.VehicleBrandRepository;
import com.mecanico.vehiclepassport.repository.VehicleDocumentRepository;
import com.mecanico.vehiclepassport.repository.VehicleModelRepository;
import com.mecanico.vehiclepassport.repository.VehicleRepository;
import com.mecanico.vehiclepassport.security.AuthUser;
import com.mecanico.vehiclepassport.security.CurrentUser;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicles;
    private final VehicleBrandRepository brands;
    private final VehicleModelRepository models;
    private final MaintenanceRecordRepository records;
    private final VehicleDocumentRepository documents;
    private final ServiceReminderRepository reminders;
    private final NotificationLogRepository notifications;
    private final VehicleAccessService access;
    private final ReminderService reminderService;
    private final HealthScoreService healthScoreService;
    private final DtoMapper mapper;
    private final CurrentUser currentUser;

    public VehicleService(VehicleRepository vehicles,
                          VehicleBrandRepository brands,
                          VehicleModelRepository models,
                          MaintenanceRecordRepository records,
                          VehicleDocumentRepository documents,
                          ServiceReminderRepository reminders,
                          NotificationLogRepository notifications,
                          VehicleAccessService access,
                          ReminderService reminderService,
                          HealthScoreService healthScoreService,
                          DtoMapper mapper,
                          CurrentUser currentUser) {
        this.vehicles = vehicles;
        this.brands = brands;
        this.models = models;
        this.records = records;
        this.documents = documents;
        this.reminders = reminders;
        this.notifications = notifications;
        this.access = access;
        this.reminderService = reminderService;
        this.healthScoreService = healthScoreService;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Transactional
    public VehicleDtos.VehicleResponse create(VehicleDtos.VehicleRequest request) {
        AuthUser user = currentUser.require();
        Long ownerId = resolveOwner(request.ownerId(), user);
        VehicleBrand brand = brands.findByIdAndActiveTrue(request.brandId())
                .orElseThrow(() -> ApiException.badRequest("Unknown vehicle brand"));
        VehicleModel model = models.findByIdAndBrandIdAndActiveTrue(request.modelId(), brand.getId())
                .orElseThrow(() -> ApiException.badRequest("Unknown model for the selected brand"));

        String vin = normalizeVin(request.vin());
        String plate = normalizePlate(request.plate());
        ensureUnique(vin, plate, null);

        Vehicle vehicle = new Vehicle();
        vehicle.setOwnerId(ownerId);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        apply(vehicle, request, vin, plate);
        Vehicle saved = vehicles.save(vehicle);
        reminderService.createDefaultReminders(saved);
        return mapper.toVehicleResponse(saved);
    }

    @Transactional(readOnly = true)
    public CommonDtos.PageResponse<VehicleDtos.VehicleResponse> search(Long ownerId, String search,
                                                                       VehicleStatus status, int page, int size) {
        AuthUser user = currentUser.require();
        boolean manager = isManager(user);
        Specification<Vehicle> spec = (root, query, cb) -> cb.conjunction();
        if (!manager) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ownerId"), user.id()));
        } else if (ownerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ownerId"), ownerId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Vehicle, VehicleBrand> brandJoin = root.join("brand", JoinType.LEFT);
                Join<Vehicle, VehicleModel> modelJoin = root.join("model", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(root.get("plate")), pattern),
                        cb.like(cb.lower(root.get("vin")), pattern),
                        cb.like(cb.lower(root.get("nickname")), pattern),
                        cb.like(cb.lower(brandJoin.get("name")), pattern),
                        cb.like(cb.lower(modelJoin.get("name")), pattern)
                );
            });
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Vehicle> result = vehicles.findAll(spec, pageable);
        return CommonDtos.PageResponse.from(result.map(mapper::toVehicleResponse));
    }

    @Transactional(readOnly = true)
    public VehicleDtos.VehicleResponse get(Long vehicleId) {
        return mapper.toVehicleResponse(access.requireReadable(vehicleId));
    }

    @Transactional
    public VehicleDtos.VehicleResponse update(Long vehicleId, VehicleDtos.VehicleRequest request) {
        Vehicle vehicle = access.requireWritable(vehicleId);
        AuthUser user = currentUser.require();
        VehicleBrand brand = brands.findByIdAndActiveTrue(request.brandId())
                .orElseThrow(() -> ApiException.badRequest("Unknown vehicle brand"));
        VehicleModel model = models.findByIdAndBrandIdAndActiveTrue(request.modelId(), brand.getId())
                .orElseThrow(() -> ApiException.badRequest("Unknown model for the selected brand"));

        String vin = normalizeVin(request.vin());
        String plate = normalizePlate(request.plate());
        ensureUnique(vin, plate, vehicle.getId());

        if (request.ownerId() != null && !request.ownerId().equals(vehicle.getOwnerId())) {
            if (!isManager(user)) {
                throw ApiException.forbidden("Only an administrator or fleet manager can reassign a vehicle");
            }
            vehicle.setOwnerId(request.ownerId());
        }
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        apply(vehicle, request, vin, plate);
        return mapper.toVehicleResponse(vehicles.save(vehicle));
    }

    @Transactional
    public void delete(Long vehicleId) {
        Vehicle vehicle = access.requireWritable(vehicleId);
        records.deleteByVehicleId(vehicleId);
        documents.deleteByVehicleId(vehicleId);
        reminders.deleteByVehicleId(vehicleId);
        notifications.deleteByVehicleId(vehicleId);
        vehicles.delete(vehicle);
    }

    @Transactional
    public VehicleDtos.VehicleResponse updateMileage(Long vehicleId, int newMileage) {
        Vehicle vehicle = access.requireWritable(vehicleId);
        int current = vehicle.getMileage() == null ? 0 : vehicle.getMileage();
        if (newMileage < current) {
            throw ApiException.badRequest("The new mileage (" + newMileage
                    + " km) cannot be lower than the current mileage (" + current + " km)");
        }
        vehicle.setMileage(newMileage);
        return mapper.toVehicleResponse(vehicles.save(vehicle));
    }

    @Transactional(readOnly = true)
    public VehicleDtos.PassportResponse passport(Long vehicleId) {
        Vehicle vehicle = access.requireReadable(vehicleId);
        List<MaintenanceRecord> recordList = records.findAllByVehicleIdOrderByServiceDateDescMileageDesc(vehicleId);
        List<VehicleDocument> documentList = documents.findAllByVehicleIdOrderByExpiryDateAsc(vehicleId);
        List<ServiceReminder> reminderList = reminders.findAllByVehicleIdOrderByStatusAscNextDueMileageAsc(vehicleId);
        HealthDtos.HealthScoreResponse health = healthScoreService.compute(vehicle);

        LocalDate today = LocalDate.now();
        int expiring = 0;
        int expired = 0;
        for (VehicleDocument document : documentList) {
            if (!document.isActive()) {
                continue;
            }
            if (document.getExpiryDate().isBefore(today)) {
                expired++;
            } else if (!document.getExpiryDate().isAfter(today.plusDays(30))) {
                expiring++;
            }
        }
        int due = 0;
        int overdue = 0;
        for (ServiceReminder reminder : reminderList) {
            ReminderStatus status = ReminderEvaluator.effectiveStatus(reminder, vehicle, today);
            if (status == ReminderStatus.OVERDUE) {
                overdue++;
            } else if (status == ReminderStatus.DUE) {
                due++;
            }
        }
        BigDecimal totalCost = records.totalCostByVehicleId(vehicleId);
        VehicleDtos.PassportStats stats = new VehicleDtos.PassportStats(
                recordList.size(),
                totalCost == null ? BigDecimal.ZERO : totalCost,
                documentList.size(),
                expiring,
                expired,
                due,
                overdue,
                recordList.isEmpty() ? null : recordList.get(0).getServiceDate(),
                vehicle.getMileage() == null ? 0 : vehicle.getMileage()
        );

        return new VehicleDtos.PassportResponse(
                mapper.toVehicleResponse(vehicle),
                recordList.stream().map(mapper::toRecordResponse).toList(),
                documentList.stream().map(mapper::toDocumentResponse).toList(),
                reminderList.stream().map(mapper::toReminderResponse).toList(),
                health,
                stats
        );
    }

    @Transactional(readOnly = true)
    public HealthDtos.HealthScoreResponse health(Long vehicleId) {
        return healthScoreService.compute(access.requireReadable(vehicleId));
    }

    @Transactional(readOnly = true)
    public Set<Long> accessibleVehicleIds() {
        AuthUser user = currentUser.require();
        List<Vehicle> list = isManager(user)
                ? vehicles.findAllDetailed()
                : vehicles.findAllByOwnerIdOrderByCreatedAtDesc(user.id());
        return list.stream().map(Vehicle::getId).collect(Collectors.toSet());
    }

    private void apply(Vehicle vehicle, VehicleDtos.VehicleRequest request, String vin, String plate) {
        vehicle.setVin(vin);
        vehicle.setPlate(plate);
        vehicle.setPlateCountry(request.plateCountry() == null || request.plateCountry().isBlank()
                ? "TN" : request.plateCountry().trim().toUpperCase());
        vehicle.setYear(request.year());
        vehicle.setFuelType(request.fuelType());
        vehicle.setMileage(request.mileage());
        vehicle.setRegistrationDate(request.registrationDate());
        vehicle.setColor(trimToNull(request.color()));
        vehicle.setNickname(trimToNull(request.nickname()));
        vehicle.setVehicleType(request.vehicleType() == null
                ? com.mecanico.vehiclepassport.entity.VehicleType.CAR : request.vehicleType());
        vehicle.setTransmission(request.transmission());
        vehicle.setEngineCapacityCc(request.engineCapacityCc());
        vehicle.setPowerHp(request.powerHp());
        vehicle.setPhotoUrl(trimToNull(request.photoUrl()));
        vehicle.setNotes(trimToNull(request.notes()));
        vehicle.setStatus(request.status() == null ? VehicleStatus.ACTIVE : request.status());
    }

    private Long resolveOwner(Long requestedOwnerId, AuthUser user) {
        if (requestedOwnerId == null || requestedOwnerId.equals(user.id())) {
            return user.id();
        }
        if (!isManager(user)) {
            throw ApiException.forbidden("Only an administrator or fleet manager can create a vehicle for another owner");
        }
        return requestedOwnerId;
    }

    private void ensureUnique(String vin, String plate, Long currentVehicleId) {
        boolean vinExists = currentVehicleId == null
                ? vehicles.existsByVinIgnoreCase(vin)
                : vehicles.existsByVinIgnoreCaseAndIdNot(vin, currentVehicleId);
        if (vinExists) {
            throw ApiException.conflict("A vehicle with this VIN already exists");
        }
        boolean plateExists = currentVehicleId == null
                ? vehicles.existsByPlateIgnoreCase(plate)
                : vehicles.existsByPlateIgnoreCaseAndIdNot(plate, currentVehicleId);
        if (plateExists) {
            throw ApiException.conflict("A vehicle with this license plate already exists");
        }
    }

    private boolean isManager(AuthUser user) {
        return "ADMIN".equals(user.role()) || "FLEET_MANAGER".equals(user.role());
    }

    private String normalizeVin(String vin) {
        return vin.trim().toUpperCase();
    }

    private String normalizePlate(String plate) {
        return plate.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
