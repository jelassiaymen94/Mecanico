-- ============================================================
--  MECANICO - MS1 Vehicle Passport database
--  Database : mecanico_vehicle_passport_db
--  MySQL 8+ / MariaDB 10.4+
--  WAMP: import this file in phpMyAdmin or run:
--    mysql -u root < 02_mecanico_vehicle_passport_db.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS mecanico_vehicle_passport_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE mecanico_vehicle_passport_db;

-- ------------------------------------------------------------
-- 0. Vehicle catalogue (brands + models)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicle_brand (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(80)  NOT NULL,
  slug        VARCHAR(80)  NOT NULL,
  country     VARCHAR(80)  NULL,
  logo_url    VARCHAR(500) NULL,
  active      BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_brand_name (name),
  UNIQUE KEY uk_brand_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicle_model (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  brand_id    BIGINT       NOT NULL,
  name        VARCHAR(100) NOT NULL,
  slug        VARCHAR(120) NOT NULL,
  body_type   ENUM('SEDAN','HATCHBACK','SUV','COUPE','WAGON','PICKUP','VAN','TRUCK','MOTORCYCLE','OTHER') NULL,
  year_from   SMALLINT     NULL,
  year_to     SMALLINT     NULL,
  active      BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_model_brand_name (brand_id, name),
  KEY idx_model_brand (brand_id),
  CONSTRAINT fk_model_brand FOREIGN KEY (brand_id)
    REFERENCES vehicle_brand (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 1. vehicle
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicle (
  id                BIGINT       NOT NULL AUTO_INCREMENT,
  owner_id          BIGINT       NOT NULL COMMENT 'Logical FK -> mecanico_users_db.app_user.id',
  brand_id          BIGINT       NOT NULL,
  model_id          BIGINT       NOT NULL,
  vin               VARCHAR(17)  NOT NULL,
  plate             VARCHAR(20)  NOT NULL,
  plate_country     VARCHAR(3)   NOT NULL DEFAULT 'TN',
  model_year        SMALLINT     NOT NULL,
  fuel_type         ENUM('PETROL','DIESEL','HYBRID','ELECTRIC','LPG','CNG') NOT NULL DEFAULT 'PETROL',
  mileage           INT          NOT NULL DEFAULT 0,
  registration_date DATE         NULL,
  color             VARCHAR(40)  NULL,
  nickname          VARCHAR(80)  NULL,
  vehicle_type      ENUM('CAR','SEDAN','HATCHBACK','SUV','COUPE','WAGON','VAN','PICKUP','TRUCK','MOTORCYCLE','OTHER') NOT NULL DEFAULT 'CAR',
  transmission      ENUM('MANUAL','AUTOMATIC','SEMI_AUTOMATIC') NULL,
  engine_capacity_cc INT         NULL,
  power_hp          INT          NULL,
  photo_url         VARCHAR(500) NULL,
  notes             TEXT         NULL,
  status            ENUM('ACTIVE','IN_REPAIR','SOLD','STOLEN','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
  version           BIGINT       NOT NULL DEFAULT 0,
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_vehicle_vin (vin),
  UNIQUE KEY uk_vehicle_plate (plate),
  KEY idx_vehicle_owner (owner_id),
  KEY idx_vehicle_brand (brand_id),
  KEY idx_vehicle_model (model_id),
  KEY idx_vehicle_status (status),
  CONSTRAINT fk_vehicle_brand FOREIGN KEY (brand_id) REFERENCES vehicle_brand (id),
  CONSTRAINT fk_vehicle_model FOREIGN KEY (model_id) REFERENCES vehicle_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 2. maintenance_record (the digital service book)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS maintenance_record (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  vehicle_id      BIGINT        NOT NULL,
  type            ENUM('SERVICE','REPAIR','INSPECTION','PART_REPLACED') NOT NULL,
  service_date    DATE          NOT NULL,
  mileage         INT           NOT NULL,
  description     TEXT          NULL,
  provider_id     BIGINT        NULL COMMENT 'Logical FK -> MS4 Provider service',
  provider_name   VARCHAR(150)  NULL,
  request_id      BIGINT        NULL COMMENT 'Logical FK -> MS2 Request service',
  cost            DECIMAL(12,2) NULL,
  currency        VARCHAR(3)    NOT NULL DEFAULT 'TND',
  labor_cost      DECIMAL(12,2) NULL,
  parts_cost      DECIMAL(12,2) NULL,
  part_name       VARCHAR(150)  NULL,
  part_reference  VARCHAR(80)   NULL,
  invoice_url     VARCHAR(500)  NULL,
  next_due_date   DATE          NULL,
  next_due_mileage INT          NULL,
  created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_record_vehicle (vehicle_id),
  KEY idx_record_date (service_date),
  KEY idx_record_type (type),
  CONSTRAINT fk_record_vehicle FOREIGN KEY (vehicle_id)
    REFERENCES vehicle (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 3. vehicle_document (insurance, inspection, registration, vignette)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicle_document (
  id                 BIGINT        NOT NULL AUTO_INCREMENT,
  vehicle_id         BIGINT        NOT NULL,
  type               ENUM('INSURANCE','INSPECTION','REGISTRATION','VIGNETTE') NOT NULL,
  document_number    VARCHAR(80)   NULL,
  issuer             VARCHAR(150)  NULL,
  issue_date         DATE          NULL,
  expiry_date        DATE          NOT NULL,
  file_url           VARCHAR(500)  NULL,
  cost               DECIMAL(12,2) NULL,
  notes              TEXT          NULL,
  notification_30_sent BOOLEAN     NOT NULL DEFAULT FALSE,
  notification_7_sent  BOOLEAN     NOT NULL DEFAULT FALSE,
  active             BOOLEAN       NOT NULL DEFAULT TRUE,
  created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_document_vehicle (vehicle_id),
  KEY idx_document_expiry (expiry_date),
  KEY idx_document_type (type),
  CONSTRAINT fk_document_vehicle FOREIGN KEY (vehicle_id)
    REFERENCES vehicle (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 4. service_reminder (mileage + date based reminders)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS service_reminder (
  id                BIGINT       NOT NULL AUTO_INCREMENT,
  vehicle_id        BIGINT       NOT NULL,
  type              ENUM('OIL_CHANGE','TIRE_ROTATION','BRAKE_CHECK','BATTERY_CHECK','TECHNICAL_INSPECTION','INSURANCE_RENEWAL','CUSTOM') NOT NULL DEFAULT 'CUSTOM',
  title             VARCHAR(150) NOT NULL,
  mileage_trigger   INT          NULL,
  next_due_mileage  INT          NULL,
  expiry_date       DATE         NULL,
  status            ENUM('PENDING','DUE','OVERDUE','COMPLETED','DISMISSED') NOT NULL DEFAULT 'PENDING',
  notified          BOOLEAN      NOT NULL DEFAULT FALSE,
  last_notified_at  DATETIME     NULL,
  notes             TEXT         NULL,
  completed_at      DATETIME     NULL,
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_reminder_vehicle (vehicle_id),
  KEY idx_reminder_due_mileage (next_due_mileage),
  KEY idx_reminder_expiry (expiry_date),
  KEY idx_reminder_status (status),
  CONSTRAINT fk_reminder_vehicle FOREIGN KEY (vehicle_id)
    REFERENCES vehicle (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 5. notification_log (audit of cron / in-app alerts)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification_log (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  vehicle_id  BIGINT       NULL,
  document_id BIGINT       NULL,
  reminder_id BIGINT       NULL,
  event_type  ENUM('DOCUMENT_EXPIRING_30','DOCUMENT_EXPIRING_7','REMINDER_DUE','REMINDER_OVERDUE') NOT NULL,
  message     VARCHAR(500) NOT NULL,
  channel     ENUM('IN_APP','EMAIL','SMS') NOT NULL DEFAULT 'IN_APP',
  status      ENUM('SENT','SKIPPED','FAILED') NOT NULL DEFAULT 'SENT',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notification_vehicle (vehicle_id),
  KEY idx_notification_document (document_id),
  KEY idx_notification_reminder (reminder_id),
  KEY idx_notification_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  VEHICLE CATALOGUE (20 brands, 110 models)
-- ============================================================
INSERT INTO vehicle_brand (id, name, slug, country) VALUES
  (1,'Toyota','toyota','Japan'),
  (2,'Volkswagen','volkswagen','Germany'),
  (3,'Renault','renault','France'),
  (4,'Peugeot','peugeot','France'),
  (5,'Hyundai','hyundai','South Korea'),
  (6,'Kia','kia','South Korea'),
  (7,'Ford','ford','United States'),
  (8,'Mercedes-Benz','mercedes-benz','Germany'),
  (9,'BMW','bmw','Germany'),
  (10,'Audi','audi','Germany'),
  (11,'Nissan','nissan','Japan'),
  (12,'Fiat','fiat','Italy'),
  (13,'Dacia','dacia','Romania'),
  (14,'Citroën','citroen','France'),
  (15,'Honda','honda','Japan'),
  (16,'Škoda','skoda','Czech Republic'),
  (17,'Seat','seat','Spain'),
  (18,'Mitsubishi','mitsubishi','Japan'),
  (19,'Suzuki','suzuki','Japan'),
  (20,'Chery','chery','China')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO vehicle_model (id, brand_id, name, slug, body_type, year_from, year_to) VALUES
  (1,1,'Corolla','toyota-corolla','SEDAN',2018,2026),
  (2,1,'Yaris','toyota-yaris','HATCHBACK',2017,2026),
  (3,1,'RAV4','toyota-rav4','SUV',2019,2026),
  (4,1,'Hilux','toyota-hilux','PICKUP',2016,2026),
  (5,1,'Land Cruiser','toyota-land-cruiser','SUV',2015,2026),
  (6,1,'Camry','toyota-camry','SEDAN',2018,2026),
  (7,2,'Golf','volkswagen-golf','HATCHBACK',2013,2026),
  (8,2,'Polo','volkswagen-polo','HATCHBACK',2017,2026),
  (9,2,'Passat','volkswagen-passat','SEDAN',2015,2024),
  (10,2,'Tiguan','volkswagen-tiguan','SUV',2016,2026),
  (11,2,'Caddy','volkswagen-caddy','VAN',2015,2026),
  (12,2,'Touareg','volkswagen-touareg','SUV',2018,2026),
  (13,3,'Clio','renault-clio','HATCHBACK',2012,2026),
  (14,3,'Megane','renault-megane','SEDAN',2016,2024),
  (15,3,'Captur','renault-captur','SUV',2019,2026),
  (16,3,'Duster','renault-duster','SUV',2018,2026),
  (17,3,'Kangoo','renault-kangoo','VAN',2014,2026),
  (18,3,'Zoe','renault-zoe','HATCHBACK',2019,2026),
  (19,4,'208','peugeot-208','HATCHBACK',2019,2026),
  (20,4,'308','peugeot-308','SEDAN',2014,2026),
  (21,4,'2008','peugeot-2008','SUV',2020,2026),
  (22,4,'3008','peugeot-3008','SUV',2017,2026),
  (23,4,'Partner','peugeot-partner','VAN',2018,2026),
  (24,4,'508','peugeot-508','SEDAN',2018,2026),
  (25,5,'i10','hyundai-i10','HATCHBACK',2019,2026),
  (26,5,'i20','hyundai-i20','HATCHBACK',2014,2026),
  (27,5,'i30','hyundai-i30','HATCHBACK',2017,2026),
  (28,5,'Elantra','hyundai-elantra','SEDAN',2020,2026),
  (29,5,'Tucson','hyundai-tucson','SUV',2015,2026),
  (30,5,'Santa Fe','hyundai-santa-fe','SUV',2018,2026),
  (31,6,'Picanto','kia-picanto','HATCHBACK',2017,2026),
  (32,6,'Rio','kia-rio','HATCHBACK',2017,2026),
  (33,6,'Ceed','kia-ceed','HATCHBACK',2018,2026),
  (34,6,'Sportage','kia-sportage','SUV',2016,2026),
  (35,6,'Sorento','kia-sorento','SUV',2015,2026),
  (36,6,'K2700','kia-k2700','TRUCK',2010,2026),
  (37,7,'Fiesta','ford-fiesta','HATCHBACK',2017,2024),
  (38,7,'Focus','ford-focus','SEDAN',2015,2025),
  (39,7,'Mondeo','ford-mondeo','SEDAN',2014,2022),
  (40,7,'Kuga','ford-kuga','SUV',2020,2026),
  (41,7,'Ranger','ford-ranger','PICKUP',2016,2026),
  (42,7,'Transit','ford-transit','VAN',2014,2026),
  (43,8,'A-Class','mercedes-a-class','HATCHBACK',2018,2026),
  (44,8,'C-Class','mercedes-c-class','SEDAN',2014,2026),
  (45,8,'E-Class','mercedes-e-class','SEDAN',2016,2026),
  (46,8,'GLA','mercedes-gla','SUV',2020,2026),
  (47,8,'GLC','mercedes-glc','SUV',2015,2026),
  (48,8,'Sprinter','mercedes-sprinter','VAN',2018,2026),
  (49,9,'1 Series','bmw-1-series','HATCHBACK',2019,2026),
  (50,9,'3 Series','bmw-3-series','SEDAN',2019,2026),
  (51,9,'5 Series','bmw-5-series','SEDAN',2017,2026),
  (52,9,'X1','bmw-x1','SUV',2016,2026),
  (53,9,'X3','bmw-x3','SUV',2018,2026),
  (54,9,'X5','bmw-x5','SUV',2019,2026),
  (55,10,'A1','audi-a1','HATCHBACK',2018,2026),
  (56,10,'A3','audi-a3','HATCHBACK',2020,2026),
  (57,10,'A4','audi-a4','SEDAN',2016,2026),
  (58,10,'A6','audi-a6','SEDAN',2018,2026),
  (59,10,'Q3','audi-q3','SUV',2019,2026),
  (60,10,'Q5','audi-q5','SUV',2017,2026),
  (61,11,'Micra','nissan-micra','HATCHBACK',2017,2023),
  (62,11,'Sunny','nissan-sunny','SEDAN',2011,2026),
  (63,11,'Qashqai','nissan-qashqai','SUV',2014,2026),
  (64,11,'X-Trail','nissan-x-trail','SUV',2018,2026),
  (65,11,'Navara','nissan-navara','PICKUP',2016,2026),
  (66,11,'Juke','nissan-juke','SUV',2019,2026),
  (67,12,'500','fiat-500','HATCHBACK',2007,2026),
  (68,12,'Panda','fiat-panda','HATCHBACK',2012,2026),
  (69,12,'Tipo','fiat-tipo','SEDAN',2016,2026),
  (70,12,'500X','fiat-500x','SUV',2015,2026),
  (71,12,'Ducato','fiat-ducato','VAN',2014,2026),
  (72,13,'Sandero','dacia-sandero','HATCHBACK',2013,2026),
  (73,13,'Logan','dacia-logan','SEDAN',2013,2026),
  (74,13,'Duster','dacia-duster','SUV',2018,2026),
  (75,13,'Jogger','dacia-jogger','WAGON',2022,2026),
  (76,13,'Spring','dacia-spring','HATCHBACK',2021,2026),
  (77,14,'C3','citroen-c3','HATCHBACK',2017,2026),
  (78,14,'C4','citroen-c4','HATCHBACK',2021,2026),
  (79,14,'C5 Aircross','citroen-c5-aircross','SUV',2019,2026),
  (80,14,'Berlingo','citroen-berlingo','VAN',2019,2026),
  (81,14,'C-Elysée','citroen-c-elysee','SEDAN',2013,2026),
  (82,15,'Civic','honda-civic','HATCHBACK',2017,2026),
  (83,15,'Accord','honda-accord','SEDAN',2018,2026),
  (84,15,'Jazz','honda-jazz','HATCHBACK',2015,2026),
  (85,15,'CR-V','honda-cr-v','SUV',2017,2026),
  (86,15,'HR-V','honda-hr-v','SUV',2015,2026),
  (87,16,'Fabia','skoda-fabia','HATCHBACK',2014,2026),
  (88,16,'Octavia','skoda-octavia','SEDAN',2013,2026),
  (89,16,'Superb','skoda-superb','SEDAN',2015,2026),
  (90,16,'Kamiq','skoda-kamiq','SUV',2019,2026),
  (91,16,'Kodiaq','skoda-kodiaq','SUV',2017,2026),
  (92,17,'Ibiza','seat-ibiza','HATCHBACK',2017,2026),
  (93,17,'Leon','seat-leon','HATCHBACK',2013,2026),
  (94,17,'Arona','seat-arona','SUV',2018,2026),
  (95,17,'Ateca','seat-ateca','SUV',2016,2026),
  (96,17,'Tarraco','seat-tarraco','SUV',2019,2026),
  (97,18,'Lancer','mitsubishi-lancer','SEDAN',2008,2018),
  (98,18,'ASX','mitsubishi-asx','SUV',2010,2026),
  (99,18,'Outlander','mitsubishi-outlander','SUV',2013,2026),
  (100,18,'L200','mitsubishi-l200','PICKUP',2015,2026),
  (101,18,'Space Star','mitsubishi-space-star','HATCHBACK',2013,2026),
  (102,19,'Swift','suzuki-swift','HATCHBACK',2017,2026),
  (103,19,'Baleno','suzuki-baleno','HATCHBACK',2016,2026),
  (104,19,'Vitara','suzuki-vitara','SUV',2015,2026),
  (105,19,'S-Cross','suzuki-s-cross','SUV',2013,2026),
  (106,19,'Jimny','suzuki-jimny','SUV',2018,2026),
  (107,20,'Tiggo 4','chery-tiggo-4','SUV',2017,2026),
  (108,20,'Tiggo 7','chery-tiggo-7','SUV',2016,2026),
  (109,20,'Tiggo 8','chery-tiggo-8','SUV',2018,2026),
  (110,20,'Arrizo 5','chery-arrizo-5','SEDAN',2016,2026)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ============================================================
--  DEMO VEHICLES
--  owner_id values match the demo users in mecanico_users_db:
--  2 = driver@mecanico.tn, 3 = driver2@mecanico.tn,
--  4 = fleet@mecanico.tn
-- ============================================================
INSERT INTO vehicle
  (id, owner_id, brand_id, model_id, vin, plate, plate_country, model_year, fuel_type,
   mileage, registration_date, color, nickname, vehicle_type, transmission,
   engine_capacity_cc, power_hp, status)
VALUES
  (1,2,1,1,'JTDBR32E420123456','123 TUN 4567','TN',2019,'PETROL',87500,'2019-03-15','Silver','Daily driver','CAR','AUTOMATIC',1600,122,'ACTIVE'),
  (2,2,2,7,'WVWZZZ1KZAW123456','234 TUN 8910','TN',2021,'DIESEL',45200,'2021-06-10','Atlantic Blue','Golfy','CAR','MANUAL',1968,150,'ACTIVE'),
  (3,3,3,13,'VF1R9801234567890','345 TUN 1122','TN',2017,'PETROL',132000,'2017-09-01','White','Clio','CAR','MANUAL',1200,75,'IN_REPAIR'),
  (4,4,4,19,'VF3CBSHZ123456789','456 TUN 3344','TN',2022,'DIESEL',38000,'2022-02-20','Elixir Red','Fleet 01','CAR','AUTOMATIC',1500,100,'ACTIVE'),
  (5,4,5,29,'KMHJ281ABC1234567','567 TUN 7788','TN',2020,'HYBRID',64000,'2020-11-05','Amazon Grey','Fleet 02','SUV','AUTOMATIC',1600,230,'ACTIVE')
ON DUPLICATE KEY UPDATE plate = VALUES(plate);

ALTER TABLE vehicle AUTO_INCREMENT = 100;

INSERT INTO maintenance_record
  (id, vehicle_id, type, service_date, mileage, description, provider_id, provider_name,
   request_id, cost, currency, labor_cost, parts_cost, part_name, part_reference,
   next_due_date, next_due_mileage)
VALUES
  (1,1,'SERVICE','2026-09-15',85000,'Oil and filter change, 20-point inspection.',5,'Garage Central Tunis',1001,180.00,'TND',70.00,110.00,'Oil filter','OIL-F-441',NULL,95000),
  (2,1,'REPAIR','2026-06-02',82000,'Front brake pads and discs replaced.',5,'Garage Central Tunis',1002,340.00,'TND',120.00,220.00,'Front brake kit','BRK-F-220',NULL,NULL),
  (3,1,'INSPECTION','2026-03-10',78000,'Annual technical inspection passed.',NULL,'Tunis Inspection Center',NULL,70.00,'TND',70.00,0.00,NULL,NULL,'2027-03-10',NULL),
  (4,1,'SERVICE','2025-11-20',72000,'Full service: engine oil, air filter, spark plugs.',5,'Garage Central Tunis',NULL,420.00,'TND',160.00,260.00,'Service kit','SRV-K-9',NULL,82000),
  (5,2,'SERVICE','2026-08-05',43000,'DSG oil service and filters.',5,'Garage Central Tunis',1003,620.00,'TND',220.00,400.00,'DSG kit','DSG-7-11',NULL,53000),
  (6,2,'PART_REPLACED','2026-04-12',40000,'Battery replaced under warranty.',5,'Garage Central Tunis',1004,280.00,'TND',30.00,250.00,'AGM Battery 70Ah','BAT-AGM-70',NULL,NULL),
  (7,3,'REPAIR','2026-10-01',130000,'Clutch kit replacement.',5,'Garage Central Tunis',1005,950.00,'TND',350.00,600.00,'Clutch kit','CLT-RN-5',NULL,NULL),
  (8,4,'SERVICE','2026-07-18',35000,'Routine service and cabin filter.',5,'Garage Central Tunis',1006,210.00,'TND',80.00,130.00,'Cabin filter','CAB-P-208',NULL,45000),
  (9,5,'SERVICE','2026-05-05',61000,'Hybrid system check and oil change.',5,'Garage Central Tunis',1007,260.00,'TND',100.00,160.00,'Oil filter','OIL-H-290',NULL,71000)
ON DUPLICATE KEY UPDATE description = VALUES(description);

ALTER TABLE maintenance_record AUTO_INCREMENT = 100;

INSERT INTO vehicle_document
  (id, vehicle_id, type, document_number, issuer, issue_date, expiry_date,
   file_url, cost, notification_30_sent, notification_7_sent, active)
VALUES
  (1,1,'INSURANCE','POL-2025-78451','STAR Assurances','2025-12-01', CURRENT_DATE + INTERVAL 18 DAY, NULL, 640.00, FALSE, FALSE, TRUE),
  (2,1,'INSPECTION','VT-2025-99120','Tunis Inspection Center','2025-11-01', CURRENT_DATE - INTERVAL 18 DAY, NULL, 70.00, TRUE, TRUE, TRUE),
  (3,1,'REGISTRATION','CG-2019-34567','Ministry of Transport','2019-03-20', '2034-03-20', NULL, 120.00, FALSE, FALSE, TRUE),
  (4,1,'VIGNETTE','VIG-2026-4410','Tunisian Tax Authority','2026-01-10', CURRENT_DATE + INTERVAL 57 DAY, NULL, 400.00, FALSE, FALSE, TRUE),
  (5,2,'INSURANCE','POL-2026-11554','GAT Assurances','2026-01-15', CURRENT_DATE + INTERVAL 45 DAY, NULL, 720.00, FALSE, FALSE, TRUE),
  (6,2,'INSPECTION','VT-2026-45781','Ariana Inspection Center','2026-05-20', CURRENT_DATE + INTERVAL 200 DAY, NULL, 70.00, FALSE, FALSE, TRUE),
  (7,2,'REGISTRATION','CG-2021-11223','Ministry of Transport','2021-06-15', '2036-06-15', NULL, 120.00, FALSE, FALSE, TRUE),
  (8,2,'VIGNETTE','VIG-2026-8821','Tunisian Tax Authority','2026-02-01', CURRENT_DATE + INTERVAL 80 DAY, NULL, 400.00, FALSE, FALSE, TRUE),
  (9,3,'INSURANCE','POL-2025-33221','COMAR Assurances','2025-11-25', CURRENT_DATE + INTERVAL 6 DAY, NULL, 580.00, TRUE, FALSE, TRUE),
  (10,3,'INSPECTION','VT-2025-77410','Tunis Inspection Center','2025-07-10', CURRENT_DATE + INTERVAL 130 DAY, NULL, 70.00, FALSE, FALSE, TRUE),
  (11,3,'REGISTRATION','CG-2017-99887','Ministry of Transport','2017-09-05', '2032-09-05', NULL, 120.00, FALSE, FALSE, TRUE),
  (12,3,'VIGNETTE','VIG-2025-1200','Tunisian Tax Authority','2025-01-20', CURRENT_DATE - INTERVAL 30 DAY, NULL, 400.00, TRUE, TRUE, TRUE),
  (13,4,'INSURANCE','POL-2026-77889','STAR Assurances','2026-01-05', CURRENT_DATE + INTERVAL 25 DAY, NULL, 690.00, FALSE, FALSE, TRUE),
  (14,4,'INSPECTION','VT-2026-11234','Ariana Inspection Center','2026-08-10', CURRENT_DATE + INTERVAL 300 DAY, NULL, 70.00, FALSE, FALSE, TRUE),
  (15,4,'REGISTRATION','CG-2022-55667','Ministry of Transport','2022-02-25', '2037-02-25', NULL, 120.00, FALSE, FALSE, TRUE),
  (16,4,'VIGNETTE','VIG-2026-3300','Tunisian Tax Authority','2026-02-15', CURRENT_DATE + INTERVAL 100 DAY, NULL, 400.00, FALSE, FALSE, TRUE),
  (17,5,'INSURANCE','POL-2026-99001','GAT Assurances','2026-03-01', CURRENT_DATE + INTERVAL 90 DAY, NULL, 760.00, FALSE, FALSE, TRUE),
  (18,5,'INSPECTION','VT-2026-88990','Ariana Inspection Center','2026-09-01', CURRENT_DATE + INTERVAL 60 DAY, NULL, 70.00, FALSE, FALSE, TRUE),
  (19,5,'REGISTRATION','CG-2020-22334','Ministry of Transport','2020-11-10', '2035-11-10', NULL, 120.00, FALSE, FALSE, TRUE),
  (20,5,'VIGNETTE','VIG-2026-7788','Tunisian Tax Authority','2026-03-10', CURRENT_DATE + INTERVAL 150 DAY, NULL, 400.00, FALSE, FALSE, TRUE)
ON DUPLICATE KEY UPDATE document_number = VALUES(document_number);

ALTER TABLE vehicle_document AUTO_INCREMENT = 100;

INSERT INTO service_reminder
  (id, vehicle_id, type, title, mileage_trigger, next_due_mileage, expiry_date, status, notified, notes)
VALUES
  (1,1,'OIL_CHANGE','Oil change - every 10,000 km',10000,95000,NULL,'PENDING',FALSE,'Use 5W-30 synthetic oil.'),
  (2,1,'TECHNICAL_INSPECTION','Technical inspection renewal',NULL,NULL,CURRENT_DATE + INTERVAL 18 DAY,'PENDING',FALSE,'Book the inspection before the insurance renewal.'),
  (3,1,'INSURANCE_RENEWAL','Insurance renewal',NULL,NULL,CURRENT_DATE + INTERVAL 18 DAY,'PENDING',FALSE,'STAR Assurances - policy POL-2025-78451.'),
  (4,1,'TIRE_ROTATION','Tire rotation',10000,87000,NULL,'PENDING',FALSE,'Rotate and check tire pressure.'),
  (5,2,'OIL_CHANGE','Oil change - every 10,000 km',10000,53000,NULL,'PENDING',FALSE,NULL),
  (6,2,'BRAKE_CHECK','Brake system check',15000,52000,NULL,'PENDING',FALSE,NULL),
  (7,3,'BRAKE_CHECK','Brake pads inspection',15000,130500,NULL,'PENDING',FALSE,'Squealing noise reported by the driver.'),
  (8,3,'INSURANCE_RENEWAL','Insurance renewal',NULL,NULL,CURRENT_DATE + INTERVAL 6 DAY,'PENDING',FALSE,'COMAR - policy POL-2025-33221.'),
  (9,3,'OIL_CHANGE','Oil change - every 10,000 km',10000,140000,NULL,'PENDING',FALSE,NULL),
  (10,4,'OIL_CHANGE','Oil change - every 10,000 km',10000,45000,NULL,'PENDING',FALSE,NULL),
  (11,4,'TIRE_ROTATION','Tire rotation',10000,48000,NULL,'PENDING',FALSE,NULL),
  (12,5,'OIL_CHANGE','Oil change - every 10,000 km',10000,71000,NULL,'PENDING',FALSE,NULL),
  (13,5,'BATTERY_CHECK','Battery health check',20000,80000,NULL,'PENDING',FALSE,NULL)
ON DUPLICATE KEY UPDATE title = VALUES(title);

ALTER TABLE service_reminder AUTO_INCREMENT = 100;

-- Initial in-app notifications (generated by the 30/7 day cron in production)
INSERT INTO notification_log
  (vehicle_id, document_id, reminder_id, event_type, message, channel, status)
VALUES
  (1,1,3,'DOCUMENT_EXPIRING_30','Insurance policy POL-2025-78451 expires in 18 day(s).','IN_APP','SENT'),
  (3,9,8,'DOCUMENT_EXPIRING_7','Insurance policy POL-2025-33221 expires in 6 day(s).','IN_APP','SENT'),
  (3,NULL,7,'REMINDER_OVERDUE','Brake pads inspection is overdue (130,500 km trigger, vehicle at 132,000 km).','IN_APP','SENT');

-- ============================================================
--  Database account used by Spring Boot (optional)
-- ============================================================
-- CREATE USER IF NOT EXISTS 'mecanico'@'localhost' IDENTIFIED BY 'mecanico';
-- GRANT ALL PRIVILEGES ON mecanico_vehicle_passport_db.* TO 'mecanico'@'localhost';
-- FLUSH PRIVILEGES;
