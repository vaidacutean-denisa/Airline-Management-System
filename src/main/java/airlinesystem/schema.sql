CREATE TABLE IF NOT EXISTS airplane_models (
    id                      VARCHAR(50) PRIMARY KEY,
    model_name              VARCHAR(100) NOT NULL,
    manufacturer            VARCHAR(100) NOT NULL,
    fuel_capacity           DOUBLE NOT NULL,
    cruise_speed            DOUBLE NOT NULL,
    max_range               DOUBLE NOT NULL,
    fuel_consumption        DOUBLE NOT NULL,
    maintenance_cycles      INT NOT NULL,
    cargo_capacity          DOUBLE NOT NULL
);

-- each airplane model has a capacity per class map (Map<CabinClasses, Integer>)
CREATE TABLE IF NOT EXISTS cabin_capacities (
    model_id            VARCHAR(50),
    cabin_class         VARCHAR(50),
    capacity            INT NOT NULL,

    PRIMARY KEY (model_id, cabin_class),
    FOREIGN KEY (model_id) REFERENCES airplane_models(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS airplanes (
    tail_number                 VARCHAR(20) PRIMARY KEY,
    model_id                    VARCHAR(50) NOT NULL,
    pressurization_cycles       INT NOT NULL DEFAULT 0,
    last_revision_cycles        INT NOT NULL DEFAULT 0,
    airplane_status             VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    FOREIGN KEY (model_id) REFERENCES airplane_models(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS countries (
    iso_code            VARCHAR(3) PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    economic_block      VARCHAR(50),

    CHECK (LENGTH(iso_code) = 3 AND iso_code = UPPER(iso_code))
);

-- each Country object has a list of official languages; we cannot store lists in sql -> auxiliary table
CREATE TABLE IF NOT EXISTS country_languages (
    country_iso_code        VARCHAR(3),
    language_name           VARCHAR(50),

    PRIMARY KEY (country_iso_code, language_name),
    FOREIGN KEY (country_iso_code) REFERENCES countries(iso_code)
);

CREATE TABLE IF NOT EXISTS cities (
    id                      VARCHAR(50) PRIMARY KEY,
    name                    VARCHAR(100) NOT NULL,
    country_iso_code        VARCHAR(3) NOT NULL,
    timezone                VARCHAR(50) NOT NULL,

    FOREIGN KEY (country_iso_code) REFERENCES countries(iso_code)
);

CREATE TABLE IF NOT EXISTS airports (
    id          VARCHAR(10) PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    city_id     VARCHAR(50) NOT NULL,

    FOREIGN KEY (city_id) REFERENCES cities(id),
    CHECK (LENGTH(id) = 3 AND id = UPPER(id))
);

CREATE TABLE IF NOT EXISTS routes (
    id                              VARCHAR(50) PRIMARY KEY,
    departure_airport_id            VARCHAR(10) NOT NULL,
    arrival_airport_id              VARCHAR(10) NOT NULL,
    distance_km                     DOUBLE NOT NULL,
    estimated_duration_minutes      INT NOT NULL,

    FOREIGN KEY (departure_airport_id) REFERENCES airports(id) ON DELETE CASCADE,
    FOREIGN KEY (arrival_airport_id) REFERENCES airports(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS flights (
    id                      VARCHAR(50) PRIMARY KEY,
    route_id                VARCHAR(50) NOT NULL,
    airplane_tail_number    VARCHAR(20) NOT NULL,
    departure_time          DATETIME NOT NULL,
    status                  VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',

    FOREIGN KEY (route_id) REFERENCES routes(id),
    FOREIGN KEY (airplane_tail_number) REFERENCES airplanes(tail_number)
);

-- 'people' corresponds to the person model
CREATE TABLE IF NOT EXISTS people (
    id                  INT PRIMARY KEY,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    date_of_birth       DATE NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    phone_number        VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS person_nationalities (
    person_id               INT,
    country_iso_code        VARCHAR(3),

    PRIMARY KEY (person_id, country_iso_code),
    FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE,
    FOREIGN KEY (country_iso_code) REFERENCES countries(iso_code) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS identity_documents (
    document_number             VARCHAR(50) PRIMARY KEY,
    person_id                   INT NOT NULL,
    document_type               VARCHAR(50) NOT NULL,
    expiry_date                 DATE NOT NULL,
    issuing_country_iso_code    VARCHAR(3),

    FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE,
    FOREIGN KEY (issuing_country_iso_code) REFERENCES countries(iso_code)
);

CREATE TABLE IF NOT EXISTS passengers (
    person_id               INT PRIMARY KEY,
    needs_assistance        BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employees (
    person_id           INT PRIMARY KEY,
    employee_id         VARCHAR(50) UNIQUE NOT NULL,
    hire_date           DATE NOT NULL,
    salary              DOUBLE NOT NULL,

    FOREIGN KEY (person_id) REFERENCES people(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS check_in_agents (
    employee_person_id          INT PRIMARY KEY,
    assigned_airport_id         VARCHAR(10) NOT NULL,

    FOREIGN KEY (employee_person_id) REFERENCES employees(person_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_airport_id) REFERENCES airports(id)
);

CREATE TABLE IF NOT EXISTS pilots (
    employee_person_id          INT PRIMARY KEY,
    license_number              VARCHAR(50) UNIQUE NOT NULL,

    FOREIGN KEY (employee_person_id) REFERENCES employees(person_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pilot_certifications (
    pilot_person_id             INT,
    airplane_model_id           VARCHAR(50),
    expiry_date                 DATE NOT NULL,

    PRIMARY KEY (pilot_person_id, airplane_model_id),
    FOREIGN KEY (pilot_person_id) REFERENCES pilots(employee_person_id) ON DELETE CASCADE,
    FOREIGN KEY (airplane_model_id) REFERENCES airplane_models(id)
);

CREATE TABLE IF NOT EXISTS flight_attendants (
    employee_person_id         INT PRIMARY KEY,

    FOREIGN KEY (employee_person_id) REFERENCES employees(person_id) ON DELETE CASCADE
);

-- separated tables for the languages spoken by the check-in agents and the flight attendants (otherwise would have to check role)
CREATE TABLE IF NOT EXISTS check_in_agent_languages (
    check_in_agent_person_id INT,
    language_name VARCHAR(50),

    PRIMARY KEY (check_in_agent_person_id, language_name),
    FOREIGN KEY (check_in_agent_person_id) REFERENCES check_in_agents(employee_person_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS flight_attendant_languages (
    flight_attendant_person_id INT,
    language_name VARCHAR(50),

    PRIMARY KEY (flight_attendant_person_id, language_name),
    FOREIGN KEY (flight_attendant_person_id) REFERENCES flight_attendants(employee_person_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS flight_assignments (
    employee_person_id          INT,
    flight_id                   VARCHAR(50),
    assignment_role             VARCHAR(50) NOT NULL,

    PRIMARY KEY (employee_person_id, flight_id),
    FOREIGN KEY (employee_person_id) REFERENCES employees(person_id) ON DELETE CASCADE,
    FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings (
    ticket_id               VARCHAR(50) PRIMARY KEY,
    flight_id               VARCHAR(50) NOT NULL,
    passenger_person_id     INT NOT NULL,
    cabin_class             VARCHAR(50) NOT NULL,
    luggage_weight          DOUBLE NOT NULL DEFAULT 0,

    FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE,
    FOREIGN KEY (passenger_person_id) REFERENCES passengers(person_id) ON DELETE CASCADE
);

-- for testing (we use insert ignore so it doesn't break when ran multiple times)

INSERT IGNORE INTO airplane_models (id, model_name, manufacturer, fuel_capacity, cruise_speed, max_range, fuel_consumption, maintenance_cycles, cargo_capacity) VALUES
('B738', 'Boeing 737-800', 'Boeing', 26020, 842, 5765, 3.20, 800, 10500),
('A320', 'Airbus A320', 'Airbus', 26730, 833, 6100, 21.00, 750, 9400),
('A388', 'Airbus A380-800', 'Airbus', 320000, 903, 15200, 3.30, 1500, 38000),
('AT76', 'ATR 72-600', 'ATR', 5000, 510, 1500, 4.5, 500, 1500);

INSERT IGNORE INTO cabin_capacities (model_id, cabin_class, capacity) VALUES
('B738', 'ECONOMY', 150),
('B738', 'BUSINESS', 12),
('A320', 'ECONOMY', 160),
('A320', 'BUSINESS', 20),
('A388', 'ECONOMY', 420),
('A388', 'BUSINESS', 70),
('A388', 'FIRST_CLASS', 14),
('AT76', 'ECONOMY', 72);

INSERT IGNORE INTO airplanes (tail_number, model_id, pressurization_cycles, last_revision_cycles, airplane_status) VALUES
('YR-LRC', 'A320', 1240, 1200, 'ACTIVE'),
('YR-BMS', 'B738', 150, 0, 'ACTIVE'),
('YR-ACA', 'AT76', 6200, 6000, 'ACTIVE'),
('YR-AIR', 'A388', 240, 120, 'ACTIVE');

INSERT IGNORE INTO countries (iso_code, name, economic_block) VALUES
('ROU', 'Romania', 'EU'),
('FRA', 'France', 'EU'),
('USA', 'United States', 'USMCA'),
('SGP', 'Singapore', 'ASEAN');

INSERT IGNORE INTO country_languages (country_iso_code, language_name) VALUES
('ROU', 'Romanian'),
('ROU', 'English'),
('FRA', 'French'),
('FRA', 'English'),
('USA', 'English'),
('SGP', 'English'),
('SGP', 'Mandarin');

INSERT IGNORE INTO cities (id, name, country_iso_code, timezone) VALUES
('BUH', 'Bucharest', 'ROU', 'Europe/Bucharest'),
('PAR', 'Paris', 'FRA', 'Europe/Paris'),
('NYC', 'New York', 'USA', 'America/New_York'),
('SIN', 'Singapore', 'SGP', 'Asia/Singapore');

INSERT IGNORE INTO airports (id, name, city_id) VALUES
('OTP', 'Henri Coanda International Airport', 'BUH'),
('CDG', 'Charles de Gaulle Airport', 'PAR'),
('JFK', 'John F. Kennedy International Airport', 'NYC'),
('SIN', 'Singapore Changi Airport', 'SIN');

INSERT IGNORE INTO routes (id, departure_airport_id, arrival_airport_id, distance_km, estimated_duration_minutes) VALUES
('R-OTP-CDG', 'OTP', 'CDG', 1850, 180),
('R-CDG-OTP', 'CDG', 'OTP', 1850, 180),
('R-CDG-JFK', 'CDG', 'JFK', 5840, 480),
('R-OTP-SIN', 'OTP', 'SIN', 8800, 720);

INSERT IGNORE INTO flights (id, route_id, airplane_tail_number, departure_time, status) VALUES
('FL100', 'R-OTP-CDG', 'YR-LRC', '2026-07-01 09:00:00', 'SCHEDULED'),
('FL101', 'R-CDG-OTP', 'YR-BMS', '2026-07-01 15:30:00', 'SCHEDULED'),
('FL200', 'R-CDG-JFK', 'YR-AIR', '2026-07-02 11:00:00', 'SCHEDULED'),
('FL300', 'R-OTP-SIN', 'YR-AIR', '2026-07-03 22:00:00', 'SCHEDULED');

INSERT IGNORE INTO people (id, first_name, last_name, date_of_birth, email, phone_number) VALUES
(1, 'Anna', 'Karenina', '1990-05-12', 'anna.karenina@example.com', '+40721111111'),
(2, 'Elizabeth', 'Bennet', '1988-03-22', 'eliza_bennet@example.com', '+40722222222'),
(3, 'Jean', 'Valjean', '1985-11-10', 'valjean@example.com', '+33111111111'),
(4, 'Alexei', 'Vronski', '1995-09-18', 'vronski@example.com', '+40723333333'),
(5, 'Nikolayevich', 'Myshkin', '1992-06-05', 'lev_myshkin@example.com', '+40725555555'),
(6, 'Cristiano', 'Ronaldo', '1982-01-30', 'cr7@example.com', '+40724444444');

INSERT IGNORE INTO person_nationalities (person_id, country_iso_code) VALUES
(1, 'ROU'),
(2, 'ROU'),
(3, 'FRA'),
(4, 'ROU'),
(5, 'ROU'),
(6, 'ROU');

INSERT IGNORE INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES
('PASS-RO-001', 1, 'PASSPORT', '2030-05-12', 'ROU'),
('ID-RO-002', 2, 'NATIONAL_ID', '2029-03-22', 'ROU'),
('PASS-FR-003', 3, 'PASSPORT', '2031-11-10', 'FRA'),
('PASS-RO-004', 4, 'PASSPORT', '2032-09-18', 'ROU'),
('PASS-RO-005', 5, 'PASSPORT', '2030-01-30', 'ROU'),
('PASS-RO-006', 6, 'PASSPORT', '2031-06-05', 'ROU');

INSERT IGNORE INTO passengers (person_id, needs_assistance) VALUES
(1, FALSE),
(2, TRUE),
(3, FALSE);

INSERT IGNORE INTO employees (person_id, employee_id, hire_date, salary) VALUES
(4, 'PIL-001', '2020-04-01', 8500),
(5, 'PIL-002', '2018-08-15', 9000),
(6, 'FA-001', '2021-02-10', 4200);

INSERT IGNORE INTO pilots (employee_person_id, license_number) VALUES
(4, 'LIC-PIL-001'),
(5, 'LIC-PIL-002');

INSERT IGNORE INTO pilot_certifications (pilot_person_id, airplane_model_id, expiry_date) VALUES
(4, 'A320', '2028-12-31'),
(4, 'B738', '2028-12-31'),
(5, 'A388', '2029-12-31'),
(5, 'A320', '2029-12-31');

INSERT IGNORE INTO flight_attendants (employee_person_id) VALUES
(6);

INSERT IGNORE INTO flight_attendant_languages (flight_attendant_person_id, language_name) VALUES
(6, 'Romanian'),
(6, 'English');

INSERT IGNORE INTO flight_assignments (employee_person_id, flight_id, assignment_role) VALUES
(4, 'FL100', 'CAPTAIN'),
(5, 'FL100', 'FIRST_OFFICER'),
(6, 'FL100', 'FLIGHT_ATTENDANT');

INSERT IGNORE INTO bookings (ticket_id, flight_id, passenger_person_id, cabin_class, luggage_weight) VALUES
('TCK-001', 'FL100', 1, 'ECONOMY', 18.5),
('TCK-002', 'FL100', 2, 'BUSINESS', 12.0),
('TCK-003', 'FL200', 3, 'ECONOMY', 20.0);


