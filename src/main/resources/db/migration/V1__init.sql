CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    google_id VARCHAR(255) UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE doctor_profiles (
    id CHAR(36) PRIMARY KEY,
    specialty VARCHAR(255) NOT NULL,
    bio TEXT,
    price_range VARCHAR(100),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_doctor_user FOREIGN KEY (id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE patient_profiles (
    id CHAR(36) PRIMARY KEY,
    birth_date DATE,
    anonymous_mode BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_patient_user FOREIGN KEY (id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE appointments (
    id CHAR(36) PRIMARY KEY,
    doctor_id CHAR(36) NOT NULL,
    patient_id CHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    meeting_url VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profiles(id),
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(id)
) ENGINE=InnoDB;

CREATE TABLE subscriptions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    plan VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    started_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    payment_id VARCHAR(100),
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_appointments_doctor_start ON appointments(doctor_id, start_at);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_subscriptions_user_status ON subscriptions(user_id, status);