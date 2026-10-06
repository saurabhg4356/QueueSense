-- ============================================================
-- QueueSense - Intelligent Queue & Counter Allocation System
-- Normalized MySQL Database Schema
-- Compatible with MySQL 8.0+ and MariaDB 10.4+
-- ============================================================

CREATE DATABASE IF NOT EXISTS queuesense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE queuesense;

-- Drop existing tables in reverse dependency order if needed
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS token_transfers;
DROP TABLE IF EXISTS service_pauses;
DROP TABLE IF EXISTS service_sessions;
DROP TABLE IF EXISTS token_status_history;
DROP TABLE IF EXISTS tokens;
DROP TABLE IF EXISTS aging_rules;
DROP TABLE IF EXISTS priority_rules;
DROP TABLE IF EXISTS counter_services;
DROP TABLE IF EXISTS counters;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

-- ------------------------------------------------------------
-- 1. USERS TABLE
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'OPERATOR', 'COUNTER_AGENT', 'CUSTOMER') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_active (active)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. CUSTOMERS TABLE
-- ------------------------------------------------------------
CREATE TABLE customers (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    customer_number VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(25) NOT NULL,
    date_of_birth DATE NULL,
    special_assistance_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_customer_number (customer_number),
    INDEX idx_customer_phone (phone),
    INDEX idx_customer_user_id (user_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. SERVICES TABLE
-- ------------------------------------------------------------
CREATE TABLE services (
    service_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_code VARCHAR(50) NOT NULL UNIQUE,
    service_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    average_service_minutes INT NOT NULL DEFAULT 10,
    priority_weight INT NOT NULL DEFAULT 10,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_service_active (active),
    INDEX idx_service_code (service_code)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. COUNTERS TABLE
-- ------------------------------------------------------------
CREATE TABLE counters (
    counter_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    counter_number INT NOT NULL UNIQUE,
    counter_name VARCHAR(50) NOT NULL,
    location VARCHAR(100) NULL,
    status ENUM('AVAILABLE', 'BUSY', 'BREAK', 'OFFLINE') NOT NULL DEFAULT 'OFFLINE',
    assigned_agent_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_counter_agent FOREIGN KEY (assigned_agent_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_counter_status (status),
    INDEX idx_counter_number (counter_number)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. COUNTER-SERVICE COMPATIBILITY
-- ------------------------------------------------------------
CREATE TABLE counter_services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    counter_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cs_counter FOREIGN KEY (counter_id) REFERENCES counters(counter_id) ON DELETE CASCADE,
    CONSTRAINT fk_cs_service FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE,
    CONSTRAINT uk_counter_service UNIQUE (counter_id, service_id),
    INDEX idx_cs_counter (counter_id),
    INDEX idx_cs_service (service_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. PRIORITY RULES
-- ------------------------------------------------------------
CREATE TABLE priority_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    priority_type ENUM('NORMAL', 'PRIORITY', 'URGENT', 'EMERGENCY') NOT NULL UNIQUE,
    base_weight INT NOT NULL,
    description VARCHAR(255) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. AGING RULES
-- ------------------------------------------------------------
CREATE TABLE aging_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    minutes_interval INT NOT NULL DEFAULT 10,
    bonus_points INT NOT NULL DEFAULT 5,
    maximum_bonus INT NOT NULL DEFAULT 50,
    starvation_threshold_minutes INT NOT NULL DEFAULT 30,
    special_assistance_bonus INT NOT NULL DEFAULT 15,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 8. TOKENS TABLE
-- ------------------------------------------------------------
CREATE TABLE tokens (
    token_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_number VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    priority_type ENUM('NORMAL', 'PRIORITY', 'URGENT', 'EMERGENCY') NOT NULL DEFAULT 'NORMAL',
    priority_score DOUBLE NOT NULL DEFAULT 0.0,
    status ENUM('WAITING', 'CALLED', 'SERVING', 'PAUSED', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'TRANSFERRED') NOT NULL DEFAULT 'WAITING',
    counter_id BIGINT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_at TIMESTAMP NULL,
    service_started_at TIMESTAMP NULL,
    service_completed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancelled_by BIGINT NULL,
    cancellation_reason VARCHAR(255) NULL,
    no_show_at TIMESTAMP NULL,
    estimated_wait_minutes INT NOT NULL DEFAULT 0,
    actual_wait_minutes INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    CONSTRAINT fk_token_service FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE RESTRICT,
    CONSTRAINT fk_token_counter FOREIGN KEY (counter_id) REFERENCES counters(counter_id) ON DELETE SET NULL,
    CONSTRAINT fk_token_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_token_number (token_number),
    INDEX idx_token_status (status),
    INDEX idx_token_priority (priority_type),
    INDEX idx_token_issued_at (issued_at),
    INDEX idx_token_service (service_id),
    INDEX idx_token_counter (counter_id),
    INDEX idx_token_customer (customer_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 9. TOKEN STATUS HISTORY
-- ------------------------------------------------------------
CREATE TABLE token_status_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_id BIGINT NOT NULL,
    old_status ENUM('WAITING', 'CALLED', 'SERVING', 'PAUSED', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'TRANSFERRED') NULL,
    new_status ENUM('WAITING', 'CALLED', 'SERVING', 'PAUSED', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'TRANSFERRED') NOT NULL,
    changed_by BIGINT NULL,
    remarks VARCHAR(255) NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tsh_token FOREIGN KEY (token_id) REFERENCES tokens(token_id) ON DELETE CASCADE,
    CONSTRAINT fk_tsh_user FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_tsh_token (token_id),
    INDEX idx_tsh_changed_at (changed_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 10. SERVICE SESSIONS
-- ------------------------------------------------------------
CREATE TABLE service_sessions (
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_id BIGINT NOT NULL,
    counter_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paused_at TIMESTAMP NULL,
    resumed_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    actual_duration_seconds INT NOT NULL DEFAULT 0,
    remarks TEXT NULL,
    CONSTRAINT fk_ss_token FOREIGN KEY (token_id) REFERENCES tokens(token_id) ON DELETE CASCADE,
    CONSTRAINT fk_ss_counter FOREIGN KEY (counter_id) REFERENCES counters(counter_id) ON DELETE RESTRICT,
    INDEX idx_ss_token (token_id),
    INDEX idx_ss_counter (counter_id),
    INDEX idx_ss_started_at (started_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 11. SERVICE PAUSES
-- ------------------------------------------------------------
CREATE TABLE service_pauses (
    pause_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    pause_reason ENUM('DOCUMENT_VERIFICATION', 'CUSTOMER_REQUEST', 'SUPERVISOR_APPROVAL', 'SYSTEM_ISSUE', 'OTHER') NOT NULL,
    paused_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resumed_at TIMESTAMP NULL,
    duration_seconds INT NOT NULL DEFAULT 0,
    remarks VARCHAR(255) NULL,
    CONSTRAINT fk_sp_session FOREIGN KEY (session_id) REFERENCES service_sessions(session_id) ON DELETE CASCADE,
    INDEX idx_sp_session (session_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 12. TOKEN TRANSFERS
-- ------------------------------------------------------------
CREATE TABLE token_transfers (
    transfer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_id BIGINT NOT NULL,
    from_counter_id BIGINT NOT NULL,
    to_counter_id BIGINT NOT NULL,
    reason ENUM('SPECIALIZED_SERVICE_REQUIRED', 'AGENT_UNAVAILABLE', 'COUNTER_CLOSING', 'TECHNICAL_PROBLEM', 'CUSTOMER_REQUEST', 'OTHER') NOT NULL,
    transferred_by BIGINT NOT NULL,
    transferred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255) NULL,
    CONSTRAINT fk_tt_token FOREIGN KEY (token_id) REFERENCES tokens(token_id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_from FOREIGN KEY (from_counter_id) REFERENCES counters(counter_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tt_to FOREIGN KEY (to_counter_id) REFERENCES counters(counter_id) ON DELETE RESTRICT,
    CONSTRAINT fk_tt_user FOREIGN KEY (transferred_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_tt_token (token_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 13. FEEDBACK TABLE
-- ------------------------------------------------------------
CREATE TABLE feedback (
    feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fb_token FOREIGN KEY (token_id) REFERENCES tokens(token_id) ON DELETE CASCADE,
    CONSTRAINT fk_fb_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    INDEX idx_fb_rating (rating),
    INDEX idx_fb_created (created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 14. AUDIT LOGS TABLE
-- ------------------------------------------------------------
CREATE TABLE audit_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NULL,
    details TEXT NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_audit_action (action),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;
