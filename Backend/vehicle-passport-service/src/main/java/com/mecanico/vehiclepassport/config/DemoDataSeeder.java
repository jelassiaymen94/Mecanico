package com.mecanico.vehiclepassport.config;

import com.mecanico.vehiclepassport.entity.BodyType;
import com.mecanico.vehiclepassport.entity.DocumentType;
import com.mecanico.vehiclepassport.entity.FuelType;
import com.mecanico.vehiclepassport.entity.MaintenanceRecord;
import com.mecanico.vehiclepassport.entity.NotificationChannel;
import com.mecanico.vehiclepassport.entity.NotificationEventType;
import com.mecanico.vehiclepassport.entity.NotificationLog;
import com.mecanico.vehiclepassport.entity.NotificationStatus;
import com.mecanico.vehiclepassport.entity.RecordType;
import com.mecanico.vehiclepassport.entity.ReminderType;
import com.mecanico.vehiclepassport.entity.ServiceReminder;
import com.mecanico.vehiclepassport.entity.TransmissionType;
import com.mecanico.vehiclepassport.entity.Vehicle;
import com.mecanico.vehiclepassport.entity.VehicleBrand;
import com.mecanico.vehiclepassport.entity.VehicleDocument;
import com.mecanico.vehiclepassport.entity.VehicleModel;
import com.mecanico.vehiclepassport.entity.VehicleStatus;
import com.mecanico.vehiclepassport.entity.VehicleType;
import com.mecanico.vehiclepassport.repository.MaintenanceRecordRepository;
import com.mecanico.vehiclepassport.repository.NotificationLogRepository;
import com.mecanico.vehiclepassport.repository.ServiceReminderRepository;
import com.mecanico.vehiclepassport.repository.VehicleBrandRepository;
import com.mecanico.vehiclepassport.repository.VehicleDocumentRepository;
import com.mecanico.vehiclepassport.repository.VehicleModelRepository;
import com.mecanico.vehiclepassport.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final String CATALOG = """
            Toyota|Japan|Corolla:SEDAN:2018:2026;Yaris:HATCHBACK:2017:2026;RAV4:SUV:2019:2026;Hilux:PICKUP:2016:2026;Land Cruiser:SUV:2015:2026;Camry:SEDAN:2018:2026
            Volkswagen|Germany|Golf:HATCHBACK:2013:2026;Polo:HATCHBACK:2017:2026;Passat:SEDAN:2015:2024;Tiguan:SUV:2016:2026;Caddy:VAN:2015:2026;Touareg:SUV:2018:2026
            Renault|France|Clio:HATCHBACK:2012:2026;Megane:SEDAN:2016:2024;Captur:SUV:2019:2026;Duster:SUV:2018:2026;Kangoo:VAN:2014:2026;Zoe:HATCHBACK:2019:2026
            Peugeot|France|208:HATCHBACK:2019:2026;308:SEDAN:2014:2026;2008:SUV:2020:2026;3008:SUV:2017:2026;Partner:VAN:2018:2026;508:SEDAN:2018:2026
            Hyundai|South Korea|i10:HATCHBACK:2019:2026;i20:HATCHBACK:2014:2026;i30:HATCHBACK:2017:2026;Elantra:SEDAN:2020:2026;Tucson:SUV:2015:2026;Santa Fe:SUV:2018:2026
            Kia|South Korea|Picanto:HATCHBACK:2017:2026;Rio:HATCHBACK:2017:2026;Ceed:HATCHBACK:2018:2026;Sportage:SUV:2016:2026;Sorento:SUV:2015:2026;K2700:TRUCK:2010:2026
            Ford|United States|Fiesta:HATCHBACK:2017:2024;Focus:SEDAN:2015:2025;Mondeo:SEDAN:2014:2022;Kuga:SUV:2020:2026;Ranger:PICKUP:2016:2026;Transit:VAN:2014:2026
            Mercedes-Benz|Germany|A-Class:HATCHBACK:2018:2026;C-Class:SEDAN:2014:2026;E-Class:SEDAN:2016:2026;GLA:SUV:2020:2026;GLC:SUV:2015:2026;Sprinter:VAN:2018:2026
            BMW|Germany|1 Series:HATCHBACK:2019:2026;3 Series:SEDAN:2019:2026;5 Series:SEDAN:2017:2026;X1:SUV:2016:2026;X3:SUV:2018:2026;X5:SUV:2019:2026
            Audi|Germany|A1:HATCHBACK:2018:2026;A3:HATCHBACK:2020:2026;A4:SEDAN:2016:2026;A6:SEDAN:2018:2026;Q3:SUV:2019:2026;Q5:SUV:2017:2026
            Nissan|Japan|Micra:HATCHBACK:2017:2023;Sunny:SEDAN:2011:2026;Qashqai:SUV:2014:2026;X-Trail:SUV:2018:2026;Navara:PICKUP:2016:2026;Juke:SUV:2019:2026
            Fiat|Italy|500:HATCHBACK:2007:2026;Panda:HATCHBACK:2012:2026;Tipo:SEDAN:2016:2026;500X:SUV:2015:2026;Ducato:VAN:2014:2026
            Dacia|Romania|Sandero:HATCHBACK:2013:2026;Logan:SEDAN:2013:2026;Duster:SUV:2018:2026;Jogger:WAGON:2022:2026;Spring:HATCHBACK:2021:2026
            Citroën|France|C3:HATCHBACK:2017:2026;C4:HATCHBACK:2021:2026;C5 Aircross:SUV:2019:2026;Berlingo:VAN:2019:2026;C-Elysée:SEDAN:2013:2026
            Honda|Japan|Civic:HATCHBACK:2017:2026;Accord:SEDAN:2018:2026;Jazz:HATCHBACK:2015:2026;CR-V:SUV:2017:2026;HR-V:SUV:2015:2026
            Škoda|Czech Republic|Fabia:HATCHBACK:2014:2026;Octavia:SEDAN:2013:2026;Superb:SEDAN:2015:2026;Kamiq:SUV:2019:2026;Kodiaq:SUV:2017:2026
            Seat|Spain|Ibiza:HATCHBACK:2017:2026;Leon:HATCHBACK:2013:2026;Arona:SUV:2018:2026;Ateca:SUV:2016:2026;Tarraco:SUV:2019:2026
            Mitsubishi|Japan|Lancer:SEDAN:2008:2018;ASX:SUV:2010:2026;Outlander:SUV:2013:2026;L200:PICKUP:2015:2026;Space Star:HATCHBACK:2013:2026
            Suzuki|Japan|Swift:HATCHBACK:2017:2026;Baleno:HATCHBACK:2016:2026;Vitara:SUV:2015:2026;S-Cross:SUV:2013:2026;Jimny:SUV:2018:2026
            Chery|China|Tiggo 4:SUV:2017:2026;Tiggo 7:SUV:2016:2026;Tiggo 8:SUV:2018:2026;Arrizo 5:SEDAN:2016:2026
            """;

    private final VehicleBrandRepository brands;
    private final VehicleModelRepository models;
    private final VehicleRepository vehicles;
    private final MaintenanceRecordRepository records;
    private final VehicleDocumentRepository documents;
    private final ServiceReminderRepository reminders;
    private final NotificationLogRepository notifications;

    @Value("${mecanico.seed.enabled:true}")
    private boolean enabled;

    public DemoDataSeeder(VehicleBrandRepository brands,
                          VehicleModelRepository models,
                          VehicleRepository vehicles,
                          MaintenanceRecordRepository records,
                          VehicleDocumentRepository documents,
                          ServiceReminderRepository reminders,
                          NotificationLogRepository notifications) {
        this.brands = brands;
        this.models = models;
        this.vehicles = vehicles;
        this.records = records;
        this.documents = documents;
        this.reminders = reminders;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        seedCatalog();
        if (vehicles.count() > 0) {
            log.info("Vehicle passport demo data already present - skipping vehicle seed.");
            return;
        }
        seedVehicles();
    }

    private void seedCatalog() {
        if (brands.count() > 0) {
            return;
        }
        for (String line : CATALOG.strip().split("\\R")) {
            String[] brandParts = line.split("\\|", 3);
            if (brandParts.length < 3) {
                continue;
            }
            VehicleBrand brand = new VehicleBrand();
            brand.setName(brandParts[0].trim());
            brand.setCountry(brandParts[1].trim());
            brand.setSlug(slugify(brandParts[0]));
            brand = brands.save(brand);
            for (String modelDef : brandParts[2].split(";")) {
                String[] parts = modelDef.split(":");
                if (parts.length < 4) {
                    continue;
                }
                VehicleModel model = new VehicleModel();
                model.setBrand(brand);
                model.setName(parts[0].trim());
                model.setBodyType(BodyType.valueOf(parts[1].trim()));
                model.setYearFrom(Integer.parseInt(parts[2].trim()));
                model.setYearTo(Integer.parseInt(parts[3].trim()));
                model.setSlug(slugify(brand.getName() + "-" + parts[0]));
                models.save(model);
            }
        }
        log.info("Seeded vehicle catalogue: {} brands, {} models.", brands.count(), models.count());
    }

    private void seedVehicles() {
        LocalDate today = LocalDate.now();
        Vehicle corolla = vehicle(2L, "Toyota", "Corolla", "JTDBR32E420123456", "123 TUN 4567",
                2019, FuelType.PETROL, 87_500, LocalDate.of(2019, 3, 15), "Silver", "Daily driver",
                VehicleType.CAR, TransmissionType.AUTOMATIC, 1600, 122, VehicleStatus.ACTIVE);
        Vehicle golf = vehicle(2L, "Volkswagen", "Golf", "WVWZZZ1KZAW123456", "234 TUN 8910",
                2021, FuelType.DIESEL, 45_200, LocalDate.of(2021, 6, 10), "Atlantic Blue", "Golfy",
                VehicleType.CAR, TransmissionType.MANUAL, 1968, 150, VehicleStatus.ACTIVE);
        Vehicle clio = vehicle(3L, "Renault", "Clio", "VF1R9801234567890", "345 TUN 1122",
                2017, FuelType.PETROL, 132_000, LocalDate.of(2017, 9, 1), "White", "Clio",
                VehicleType.CAR, TransmissionType.MANUAL, 1200, 75, VehicleStatus.IN_REPAIR);
        Vehicle p208 = vehicle(4L, "Peugeot", "208", "VF3CBSHZ123456789", "456 TUN 3344",
                2022, FuelType.DIESEL, 38_000, LocalDate.of(2022, 2, 20), "Elixir Red", "Fleet 01",
                VehicleType.CAR, TransmissionType.AUTOMATIC, 1500, 100, VehicleStatus.ACTIVE);
        Vehicle tucson = vehicle(4L, "Hyundai", "Tucson", "KMHJ281ABC1234567", "567 TUN 7788",
                2020, FuelType.HYBRID, 64_000, LocalDate.of(2020, 11, 5), "Amazon Grey", "Fleet 02",
                VehicleType.SUV, TransmissionType.AUTOMATIC, 1600, 230, VehicleStatus.ACTIVE);

        if (corolla == null || golf == null || clio == null || p208 == null || tucson == null) {
            log.warn("Vehicle catalogue incomplete - demo vehicles were not fully seeded.");
            return;
        }

        record(corolla, RecordType.SERVICE, today.minusMonths(2), 85_000,
                "Oil and filter change, 20-point inspection.", "Garage Central Tunis", 180.00, 70.00, 110.00);
        record(corolla, RecordType.REPAIR, today.minusMonths(5), 82_000,
                "Front brake pads and discs replaced.", "Garage Central Tunis", 340.00, 120.00, 220.00);
        record(corolla, RecordType.INSPECTION, today.minusMonths(8), 78_000,
                "Annual technical inspection passed.", "Tunis Inspection Center", 70.00, 70.00, 0.00);
        record(golf, RecordType.SERVICE, today.minusMonths(3), 43_000,
                "DSG oil service and filters.", "Garage Central Tunis", 620.00, 220.00, 400.00);
        record(golf, RecordType.PART_REPLACED, today.minusMonths(7), 40_000,
                "Battery replaced under warranty.", "Garage Central Tunis", 280.00, 30.00, 250.00);
        record(clio, RecordType.REPAIR, today.minusDays(49), 130_000,
                "Clutch kit replacement.", "Garage Central Tunis", 950.00, 350.00, 600.00);
        record(p208, RecordType.SERVICE, today.minusMonths(4), 35_000,
                "Routine service and cabin filter.", "Garage Central Tunis", 210.00, 80.00, 130.00);
        record(tucson, RecordType.SERVICE, today.minusMonths(6), 61_000,
                "Hybrid system check and oil change.", "Garage Central Tunis", 260.00, 100.00, 160.00);

        document(corolla, DocumentType.INSURANCE, "POL-2025-78451", "STAR Assurances", today.minusMonths(11), today.plusDays(18), 640.00);
        document(corolla, DocumentType.INSPECTION, "VT-2025-99120", "Tunis Inspection Center", today.minusMonths(12), today.minusDays(18), 70.00);
        document(corolla, DocumentType.REGISTRATION, "CG-2019-34567", "Ministry of Transport", LocalDate.of(2019, 3, 20), today.plusYears(7), 120.00);
        document(corolla, DocumentType.VIGNETTE, "VIG-2026-4410", "Tunisian Tax Authority", today.minusMonths(10), today.plusDays(57), 400.00);
        document(golf, DocumentType.INSURANCE, "POL-2026-11554", "GAT Assurances", today.minusMonths(10), today.plusDays(45), 720.00);
        document(golf, DocumentType.INSPECTION, "VT-2026-45781", "Ariana Inspection Center", today.minusMonths(6), today.plusDays(200), 70.00);
        document(golf, DocumentType.REGISTRATION, "CG-2021-11223", "Ministry of Transport", LocalDate.of(2021, 6, 15), today.plusYears(9), 120.00);
        document(golf, DocumentType.VIGNETTE, "VIG-2026-8821", "Tunisian Tax Authority", today.minusMonths(9), today.plusDays(80), 400.00);
        document(clio, DocumentType.INSURANCE, "POL-2025-33221", "COMAR Assurances", today.minusMonths(12), today.plusDays(6), 580.00);
        document(clio, DocumentType.INSPECTION, "VT-2025-77410", "Tunis Inspection Center", today.minusMonths(4), today.plusDays(130), 70.00);
        document(clio, DocumentType.REGISTRATION, "CG-2017-99887", "Ministry of Transport", LocalDate.of(2017, 9, 5), today.plusYears(5), 120.00);
        document(clio, DocumentType.VIGNETTE, "VIG-2025-1200", "Tunisian Tax Authority", today.minusMonths(10), today.minusDays(30), 400.00);
        document(p208, DocumentType.INSURANCE, "POL-2026-77889", "STAR Assurances", today.minusMonths(11), today.plusDays(25), 690.00);
        document(p208, DocumentType.INSPECTION, "VT-2026-11234", "Ariana Inspection Center", today.minusMonths(3), today.plusDays(300), 70.00);
        document(p208, DocumentType.REGISTRATION, "CG-2022-55667", "Ministry of Transport", LocalDate.of(2022, 2, 25), today.plusYears(10), 120.00);
        document(p208, DocumentType.VIGNETTE, "VIG-2026-3300", "Tunisian Tax Authority", today.minusMonths(9), today.plusDays(100), 400.00);
        document(tucson, DocumentType.INSURANCE, "POL-2026-99001", "GAT Assurances", today.minusMonths(8), today.plusDays(90), 760.00);
        document(tucson, DocumentType.INSPECTION, "VT-2026-88990", "Ariana Inspection Center", today.minusMonths(2), today.plusDays(60), 70.00);
        document(tucson, DocumentType.REGISTRATION, "CG-2020-22334", "Ministry of Transport", LocalDate.of(2020, 11, 10), today.plusYears(9), 120.00);
        document(tucson, DocumentType.VIGNETTE, "VIG-2026-7788", "Tunisian Tax Authority", today.minusMonths(8), today.plusDays(150), 400.00);

        ServiceReminder corollaOil = reminder(corolla, ReminderType.OIL_CHANGE, "Oil change - every 10,000 km",
                10_000, 95_000, null, "Use 5W-30 synthetic oil.");
        reminder(corolla, ReminderType.TECHNICAL_INSPECTION, "Technical inspection renewal",
                null, null, today.plusDays(18), "Book the inspection before the insurance renewal.");
        reminder(corolla, ReminderType.INSURANCE_RENEWAL, "Insurance renewal",
                null, null, today.plusDays(18), "STAR Assurances - policy POL-2025-78451.");
        reminder(corolla, ReminderType.TIRE_ROTATION, "Tire rotation",
                10_000, 87_000, null, "Rotate and check tire pressure.");
        reminder(golf, ReminderType.OIL_CHANGE, "Oil change - every 10,000 km", 10_000, 53_000, null, null);
        reminder(golf, ReminderType.BRAKE_CHECK, "Brake system check", 15_000, 52_000, null, null);
        reminder(clio, ReminderType.BRAKE_CHECK, "Brake pads inspection", 15_000, 130_500, null,
                "Squealing noise reported by the driver.");
        reminder(clio, ReminderType.INSURANCE_RENEWAL, "Insurance renewal", null, null, today.plusDays(6),
                "COMAR - policy POL-2025-33221.");
        reminder(clio, ReminderType.OIL_CHANGE, "Oil change - every 10,000 km", 10_000, 140_000, null, null);
        reminder(p208, ReminderType.OIL_CHANGE, "Oil change - every 10,000 km", 10_000, 45_000, null, null);
        reminder(p208, ReminderType.TIRE_ROTATION, "Tire rotation", 10_000, 48_000, null, null);
        reminder(tucson, ReminderType.OIL_CHANGE, "Oil change - every 10,000 km", 10_000, 71_000, null, null);
        reminder(tucson, ReminderType.BATTERY_CHECK, "Battery health check", 20_000, 80_000, null, null);

        NotificationLog logEntry = new NotificationLog();
        logEntry.setVehicleId(corolla.getId());
        logEntry.setReminderId(corollaOil.getId());
        logEntry.setEventType(NotificationEventType.REMINDER_DUE);
        logEntry.setMessage("Oil change reminder is approaching (95,000 km).");
        logEntry.setChannel(NotificationChannel.IN_APP);
        logEntry.setStatus(NotificationStatus.SENT);
        notifications.save(logEntry);

        log.info("Seeded 5 demo vehicles with maintenance, documents and reminders. "
                + "Owner IDs: 2 (driver), 3 (driver2), 4 (fleet manager).");
    }

    private Vehicle vehicle(Long ownerId, String brandName, String modelName, String vin, String plate,
                            int year, FuelType fuel, int mileage, LocalDate registrationDate,
                            String color, String nickname, VehicleType type, TransmissionType transmission,
                            int engineCc, int powerHp, VehicleStatus status) {
        VehicleBrand brand = brands.findByNameIgnoreCase(brandName).orElse(null);
        if (brand == null) {
            return null;
        }
        VehicleModel model = models.findByBrandIdAndNameIgnoreCase(brand.getId(), modelName).orElse(null);
        if (model == null) {
            return null;
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setOwnerId(ownerId);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setVin(vin);
        vehicle.setPlate(plate);
        vehicle.setPlateCountry("TN");
        vehicle.setYear(year);
        vehicle.setFuelType(fuel);
        vehicle.setMileage(mileage);
        vehicle.setRegistrationDate(registrationDate);
        vehicle.setColor(color);
        vehicle.setNickname(nickname);
        vehicle.setVehicleType(type);
        vehicle.setTransmission(transmission);
        vehicle.setEngineCapacityCc(engineCc);
        vehicle.setPowerHp(powerHp);
        vehicle.setStatus(status);
        return vehicles.save(vehicle);
    }

    private void record(Vehicle vehicle, RecordType type, LocalDate date, int mileage, String description,
                        String provider, double cost, double labor, double parts) {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setVehicle(vehicle);
        record.setType(type);
        record.setServiceDate(date);
        record.setMileage(mileage);
        record.setDescription(description);
        record.setProviderId(5L);
        record.setProviderName(provider);
        record.setCost(BigDecimal.valueOf(cost));
        record.setLaborCost(BigDecimal.valueOf(labor));
        record.setPartsCost(BigDecimal.valueOf(parts));
        record.setCurrency("TND");
        if (type == RecordType.SERVICE) {
            record.setNextDueMileage(mileage + 10_000);
        }
        records.save(record);
    }

    private void document(Vehicle vehicle, DocumentType type, String number, String issuer,
                          LocalDate issueDate, LocalDate expiryDate, double cost) {
        VehicleDocument document = new VehicleDocument();
        document.setVehicle(vehicle);
        document.setType(type);
        document.setNumber(number);
        document.setIssuer(issuer);
        document.setIssueDate(issueDate);
        document.setExpiryDate(expiryDate);
        document.setCost(BigDecimal.valueOf(cost));
        document.setActive(!expiryDate.isBefore(LocalDate.now().minusDays(1)));
        documents.save(document);
    }

    private ServiceReminder reminder(Vehicle vehicle, ReminderType type, String title, Integer trigger,
                                     Integer nextDueMileage, LocalDate expiryDate, String notes) {
        ServiceReminder reminder = new ServiceReminder();
        reminder.setVehicle(vehicle);
        reminder.setType(type);
        reminder.setTitle(title);
        reminder.setMileageTrigger(trigger);
        reminder.setNextDueMileage(nextDueMileage);
        reminder.setExpiryDate(expiryDate);
        reminder.setNotes(notes);
        return reminders.save(reminder);
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
