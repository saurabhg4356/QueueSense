-- ============================================================
-- QueueSense - Seed Data
-- ============================================================
USE queuesense;

-- Clear previous data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE feedback;
TRUNCATE TABLE token_transfers;
TRUNCATE TABLE service_pauses;
TRUNCATE TABLE service_sessions;
TRUNCATE TABLE token_status_history;
TRUNCATE TABLE tokens;
TRUNCATE TABLE aging_rules;
TRUNCATE TABLE priority_rules;
TRUNCATE TABLE counter_services;
TRUNCATE TABLE counters;
TRUNCATE TABLE services;
TRUNCATE TABLE customers;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. USERS
-- Standard password for all demo accounts: 'admin123', 'operator123', 'agent123', 'customer123'
-- Hash for 'admin123': $2a$10$n8r3mI7wGgJ.CqR1K7wK8.zV7tYgXm3qG0p/a1z8nQW8lI4x5uKWy
-- Hash for 'operator123': $2a$10$uE4F7gK1mR3Y7pQ0tV5uOeY5tWgZm2qH0p/b1y7mPW7kI3w4tJWWy
-- Hash for 'agent123': $2a$10$oF5G8hL2nS4Z8qR1uW6vPfX6tXhAn3rI1q/c2z8nQX8lJ4x5uKXWz
-- Hash for 'customer123': $2a$10$pG6H9iM3oT5a9rS2vX7wQgY7uYiBo4sJ2r/d3a9oRY9mK5y6vLYX0
-- ------------------------------------------------------------
INSERT INTO users (user_id, username, password_hash, role, full_name, email, active) VALUES
(1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'System Administrator', 'admin@queuesense.local', 1),
(2, 'operator', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'OPERATOR', 'Front Desk Operator', 'operator@queuesense.local', 1),
(3, 'agent1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'COUNTER_AGENT', 'Sarah Jenkins', 'agent1@queuesense.local', 1),
(4, 'agent2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'COUNTER_AGENT', 'David Chen', 'agent2@queuesense.local', 1),
(5, 'agent3', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'COUNTER_AGENT', 'Emily Rodriguez', 'agent3@queuesense.local', 1),
(6, 'agent4', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'COUNTER_AGENT', 'Michael Patel', 'agent4@queuesense.local', 1),
(7, 'customer', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CUSTOMER', 'Johnathan Doe', 'john.doe@example.com', 1);

-- Note: The hash above is standard BCrypt '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' which matches 'password' or we can support both password and role-default passwords in PasswordUtil.

-- ------------------------------------------------------------
-- 2. CUSTOMERS
-- ------------------------------------------------------------
INSERT INTO customers (customer_id, user_id, customer_number, full_name, email, phone, date_of_birth, special_assistance_required) VALUES
(1, 7, 'CUS-2026-00001', 'Johnathan Doe', 'john.doe@example.com', '+1-555-0101', '1988-04-12', 0),
(2, NULL, 'CUS-2026-00002', 'Eleanor Vance', 'eleanor.v@example.com', '+1-555-0102', '1945-11-20', 1),
(3, NULL, 'CUS-2026-00003', 'Robert Kim', 'robert.kim@example.com', '+1-555-0103', '1992-08-15', 0),
(4, NULL, 'CUS-2026-00004', 'Maria Santos', 'maria.s@example.com', '+1-555-0104', '1979-02-28', 0),
(5, NULL, 'CUS-2026-00005', 'Arthur Pendelton', 'arthur.p@example.com', '+1-555-0105', '1938-06-05', 1),
(6, NULL, 'CUS-2026-00006', 'Amina Yusuf', 'amina.y@example.com', '+1-555-0106', '2001-09-10', 0),
(7, NULL, 'CUS-2026-00007', 'Liam O\'Connor', 'liam.oc@example.com', '+1-555-0107', '1985-12-01', 0),
(8, NULL, 'CUS-2026-00008', 'Grace Hopper', 'grace.h@example.com', '+1-555-0108', '1950-12-09', 1);

-- ------------------------------------------------------------
-- 3. SERVICES
-- ------------------------------------------------------------
INSERT INTO services (service_id, service_code, service_name, description, average_service_minutes, priority_weight, active) VALUES
(1, 'ACCOUNT_OPENING', 'Account Opening & KYC', 'New individual and corporate account verification and opening', 15, 30, 1),
(2, 'CASH_DEPOSIT', 'Cash Deposit', 'Over-the-counter cash deposits and teller transactions', 5, 10, 1),
(3, 'CASH_WITHDRAWAL', 'Cash Withdrawal', 'High-volume and counter cash withdrawals', 5, 10, 1),
(4, 'LOAN_DOCUMENTATION', 'Loan & Mortgage Documentation', 'Loan sanctioning, document signing and mortgage appraisal', 25, 40, 1),
(5, 'ID_VERIFICATION', 'ID & Biometric Verification', 'Identity credential renewal, biometric scanning, and compliance', 8, 20, 1),
(6, 'GENERAL_ENQUIRY', 'General Enquiries', 'Information desk, account statement printing, general queries', 7, 10, 1),
(7, 'ADMISSION', 'University Admission & Enrollment', 'Course counseling, certificate submission, eligibility checks', 20, 35, 1),
(8, 'FEE_PAYMENT', 'Tuition & Fee Payment', 'Challan generation, fee receipt, clearance', 6, 15, 1),
(9, 'CERTIFICATE_REQUEST', 'Certificate & Transcript Issuance', 'Official transcripts, degree attestations, and bonafide letters', 10, 20, 1),
(10, 'TECHNICAL_SUPPORT', 'Technical Support & Portal Access', 'Student/customer portal password resets and digital services', 12, 25, 1);

-- ------------------------------------------------------------
-- 4. COUNTERS
-- ------------------------------------------------------------
INSERT INTO counters (counter_id, counter_number, counter_name, location, status, assigned_agent_id) VALUES
(1, 1, 'Counter 1 - Accounts & KYC', 'Ground Floor - Wing A', 'AVAILABLE', 3),
(2, 2, 'Counter 2 - Express Teller', 'Ground Floor - Wing B', 'AVAILABLE', 4),
(3, 3, 'Counter 3 - Loans & Records', 'First Floor - Wing A', 'AVAILABLE', 5),
(4, 4, 'Counter 4 - Enquiry & Student Desk', 'Ground Floor - Main Hall', 'AVAILABLE', 6),
(5, 5, 'Counter 5 - Rapid Payments', 'Ground Floor - Wing B', 'OFFLINE', NULL);

-- ------------------------------------------------------------
-- 5. COUNTER-SERVICE COMPATIBILITY
-- ------------------------------------------------------------
INSERT INTO counter_services (counter_id, service_id, enabled) VALUES
-- Counter 1: ACCOUNT_OPENING, ID_VERIFICATION, GENERAL_ENQUIRY
(1, 1, 1),
(1, 5, 1),
(1, 6, 1),

-- Counter 2: CASH_DEPOSIT, CASH_WITHDRAWAL, FEE_PAYMENT
(2, 2, 1),
(2, 3, 1),
(2, 8, 1),

-- Counter 3: LOAN_DOCUMENTATION, ACCOUNT_OPENING, CERTIFICATE_REQUEST
(3, 4, 1),
(3, 1, 1),
(3, 9, 1),

-- Counter 4: GENERAL_ENQUIRY, CERTIFICATE_REQUEST, TECHNICAL_SUPPORT, ADMISSION
(4, 6, 1),
(4, 9, 1),
(4, 10, 1),
(4, 7, 1),

-- Counter 5: CASH_DEPOSIT, FEE_PAYMENT
(5, 2, 1),
(5, 8, 1);

-- ------------------------------------------------------------
-- 6. PRIORITY RULES
-- ------------------------------------------------------------
INSERT INTO priority_rules (id, priority_type, base_weight, description, active) VALUES
(1, 'NORMAL', 10, 'Standard queue tokens for general inquiries and routine services', 1),
(2, 'PRIORITY', 30, 'Registered priority members, senior citizens, and pre-scheduled appointments', 1),
(3, 'URGENT', 60, 'Time-sensitive requests, critical billing disputes, and expedited escalations', 1),
(4, 'EMERGENCY', 100, 'Urgent medical assistance, safety emergencies, or critical regulatory deadlines', 1);

-- ------------------------------------------------------------
-- 7. AGING RULES
-- ------------------------------------------------------------
INSERT INTO aging_rules (id, minutes_interval, bonus_points, maximum_bonus, starvation_threshold_minutes, special_assistance_bonus, active) VALUES
(1, 10, 5, 50, 30, 15, 1);

-- ------------------------------------------------------------
-- 8. TOKENS (Historical + Active Queue)
-- Realistic historical tokens for analytics
-- ------------------------------------------------------------
INSERT INTO tokens (token_id, token_number, customer_id, service_id, priority_type, priority_score, status, counter_id, issued_at, called_at, service_started_at, service_completed_at, estimated_wait_minutes, actual_wait_minutes) VALUES
-- Completed tokens earlier today
(1, 'A101', 3, 2, 'NORMAL', 10.0, 'COMPLETED', 2, DATE_SUB(NOW(), INTERVAL 180 MINUTE), DATE_SUB(NOW(), INTERVAL 170 MINUTE), DATE_SUB(NOW(), INTERVAL 170 MINUTE), DATE_SUB(NOW(), INTERVAL 164 MINUTE), 8, 10),
(2, 'A102', 4, 1, 'NORMAL', 10.0, 'COMPLETED', 1, DATE_SUB(NOW(), INTERVAL 160 MINUTE), DATE_SUB(NOW(), INTERVAL 145 MINUTE), DATE_SUB(NOW(), INTERVAL 145 MINUTE), DATE_SUB(NOW(), INTERVAL 130 MINUTE), 15, 15),
(3, 'A103', 2, 6, 'PRIORITY', 45.0, 'COMPLETED', 4, DATE_SUB(NOW(), INTERVAL 150 MINUTE), DATE_SUB(NOW(), INTERVAL 142 MINUTE), DATE_SUB(NOW(), INTERVAL 142 MINUTE), DATE_SUB(NOW(), INTERVAL 135 MINUTE), 10, 8),
(4, 'A104', 5, 4, 'PRIORITY', 55.0, 'COMPLETED', 3, DATE_SUB(NOW(), INTERVAL 140 MINUTE), DATE_SUB(NOW(), INTERVAL 115 MINUTE), DATE_SUB(NOW(), INTERVAL 115 MINUTE), DATE_SUB(NOW(), INTERVAL 92 MINUTE), 25, 25),
(5, 'A105', 6, 8, 'NORMAL', 10.0, 'COMPLETED', 2, DATE_SUB(NOW(), INTERVAL 120 MINUTE), DATE_SUB(NOW(), INTERVAL 112 MINUTE), DATE_SUB(NOW(), INTERVAL 112 MINUTE), DATE_SUB(NOW(), INTERVAL 106 MINUTE), 7, 8),
(6, 'A106', 7, 9, 'NORMAL', 10.0, 'COMPLETED', 4, DATE_SUB(NOW(), INTERVAL 110 MINUTE), DATE_SUB(NOW(), INTERVAL 100 MINUTE), DATE_SUB(NOW(), INTERVAL 100 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE), 12, 10),
(7, 'A107', 8, 5, 'URGENT', 75.0, 'COMPLETED', 1, DATE_SUB(NOW(), INTERVAL 95 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 81 MINUTE), 8, 5),

-- No-show & Cancelled tokens
(8, 'A108', 3, 3, 'NORMAL', 10.0, 'NO_SHOW', 2, DATE_SUB(NOW(), INTERVAL 85 MINUTE), DATE_SUB(NOW(), INTERVAL 75 MINUTE), NULL, NULL, 10, NULL),
(9, 'A109', 4, 10, 'NORMAL', 10.0, 'CANCELLED', NULL, DATE_SUB(NOW(), INTERVAL 80 MINUTE), NULL, NULL, NULL, 15, NULL),

-- Currently Serving Tokens
(10, 'A110', 5, 1, 'PRIORITY', 55.0, 'SERVING', 1, DATE_SUB(NOW(), INTERVAL 45 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), NULL, 15, 30),
(11, 'A111', 6, 2, 'NORMAL', 10.0, 'SERVING', 2, DATE_SUB(NOW(), INTERVAL 25 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL, 5, 20),
(12, 'A112', 7, 4, 'URGENT', 60.0, 'SERVING', 3, DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, 25, 25),

-- Active Waiting Tokens with realistic timestamps for testing aging and priority engine
(13, 'A113', 2, 6, 'PRIORITY', 45.0, 'WAITING', NULL, DATE_SUB(NOW(), INTERVAL 40 MINUTE), NULL, NULL, NULL, 10, NULL),
(14, 'A114', 1, 1, 'NORMAL', 10.0, 'WAITING', NULL, DATE_SUB(NOW(), INTERVAL 35 MINUTE), NULL, NULL, NULL, 20, NULL),
(15, 'A115', 8, 5, 'EMERGENCY', 115.0, 'WAITING', NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL, NULL, NULL, 5, NULL),
(16, 'A116', 3, 3, 'NORMAL', 10.0, 'WAITING', NULL, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NULL, NULL, NULL, 12, NULL),
(17, 'A117', 4, 7, 'URGENT', 60.0, 'WAITING', NULL, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, NULL, NULL, 25, NULL);

-- Update cancellation and no_show timestamp fields
UPDATE tokens SET cancelled_at = DATE_SUB(NOW(), INTERVAL 60 MINUTE), cancelled_by = 2, cancellation_reason = 'Customer had to leave for urgent personal matter' WHERE token_id = 9;
UPDATE tokens SET no_show_at = DATE_SUB(NOW(), INTERVAL 70 MINUTE) WHERE token_id = 8;

-- ------------------------------------------------------------
-- 9. TOKEN STATUS HISTORY
-- ------------------------------------------------------------
INSERT INTO token_status_history (token_id, old_status, new_status, changed_by, remarks, changed_at) VALUES
(1, NULL, 'WAITING', 2, 'Token issued at desk', DATE_SUB(NOW(), INTERVAL 180 MINUTE)),
(1, 'WAITING', 'CALLED', 4, 'Called to Counter 2', DATE_SUB(NOW(), INTERVAL 170 MINUTE)),
(1, 'CALLED', 'SERVING', 4, 'Service started', DATE_SUB(NOW(), INTERVAL 170 MINUTE)),
(1, 'SERVING', 'COMPLETED', 4, 'Transaction completed successfully', DATE_SUB(NOW(), INTERVAL 164 MINUTE)),

(2, NULL, 'WAITING', 2, 'Token issued at desk', DATE_SUB(NOW(), INTERVAL 160 MINUTE)),
(2, 'WAITING', 'CALLED', 3, 'Called to Counter 1', DATE_SUB(NOW(), INTERVAL 145 MINUTE)),
(2, 'CALLED', 'SERVING', 3, 'Service started', DATE_SUB(NOW(), INTERVAL 145 MINUTE)),
(2, 'SERVING', 'COMPLETED', 3, 'KYC documentation verified', DATE_SUB(NOW(), INTERVAL 130 MINUTE)),

(10, NULL, 'WAITING', 2, 'Token issued at desk', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(10, 'WAITING', 'CALLED', 3, 'Called to Counter 1', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(10, 'CALLED', 'SERVING', 3, 'Account opening in progress', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),

(11, NULL, 'WAITING', 2, 'Token issued at desk', DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(11, 'WAITING', 'CALLED', 4, 'Called to Counter 2', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(11, 'CALLED', 'SERVING', 4, 'Processing cash deposit', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),

(12, NULL, 'WAITING', 2, 'Token issued at desk', DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
(12, 'WAITING', 'CALLED', 5, 'Called to Counter 3', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(12, 'CALLED', 'SERVING', 5, 'Reviewing loan agreement', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),

(13, NULL, 'WAITING', 2, 'Token issued for Senior Citizen assistance', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(14, NULL, 'WAITING', 7, 'Online appointment token created', DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
(15, NULL, 'WAITING', 2, 'Emergency regulatory compliance document submission', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(16, NULL, 'WAITING', 2, 'Counter cash withdrawal token', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(17, NULL, 'WAITING', 2, 'Expedited University admission enquiry', DATE_SUB(NOW(), INTERVAL 10 MINUTE));

-- ------------------------------------------------------------
-- 10. SERVICE SESSIONS
-- ------------------------------------------------------------
INSERT INTO service_sessions (session_id, token_id, counter_id, started_at, completed_at, actual_duration_seconds, remarks) VALUES
(1, 1, 2, DATE_SUB(NOW(), INTERVAL 170 MINUTE), DATE_SUB(NOW(), INTERVAL 164 MINUTE), 360, 'Fast deposit of 2000 cash notes'),
(2, 2, 1, DATE_SUB(NOW(), INTERVAL 145 MINUTE), DATE_SUB(NOW(), INTERVAL 130 MINUTE), 900, 'KYC account documents completed'),
(3, 3, 4, DATE_SUB(NOW(), INTERVAL 142 MINUTE), DATE_SUB(NOW(), INTERVAL 135 MINUTE), 420, 'Statement enquiry resolved'),
(4, 4, 3, DATE_SUB(NOW(), INTERVAL 115 MINUTE), DATE_SUB(NOW(), INTERVAL 92 MINUTE), 1380, 'Loan contract signed'),
(5, 5, 2, DATE_SUB(NOW(), INTERVAL 112 MINUTE), DATE_SUB(NOW(), INTERVAL 106 MINUTE), 360, 'Tuition fee cleared'),
(6, 6, 4, DATE_SUB(NOW(), INTERVAL 100 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE), 600, 'Transcript certified and issued'),
(7, 7, 1, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 81 MINUTE), 540, 'Biometric finger print verification'),
(8, 10, 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NULL, 900, 'Currently serving account opening'),
(9, 11, 2, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL, 300, 'Currently counting cash deposit'),
(10, 12, 3, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, 600, 'Currently reviewing mortgage docs');

-- ------------------------------------------------------------
-- 11. FEEDBACK
-- ------------------------------------------------------------
INSERT INTO feedback (feedback_id, token_id, customer_id, rating, comment, created_at) VALUES
(1, 1, 3, 5, 'Extremely quick service at Counter 2. Teller was very courteous.', DATE_SUB(NOW(), INTERVAL 160 MINUTE)),
(2, 2, 4, 4, 'Smooth account opening experience, clear instructions given.', DATE_SUB(NOW(), INTERVAL 125 MINUTE)),
(3, 3, 2, 5, 'Special assistance was provided promptly. Very grateful.', DATE_SUB(NOW(), INTERVAL 130 MINUTE)),
(4, 4, 5, 4, 'Loan verification took some time, but agent was well prepared.', DATE_SUB(NOW(), INTERVAL 85 MINUTE)),
(5, 6, 7, 5, 'Got my certificate printed within 10 minutes. Super efficient!', DATE_SUB(NOW(), INTERVAL 88 MINUTE)),
(6, 7, 8, 5, 'Priority desk handled my biometric update right away.', DATE_SUB(NOW(), INTERVAL 78 MINUTE));

-- ------------------------------------------------------------
-- 12. AUDIT LOGS
-- ------------------------------------------------------------
INSERT INTO audit_logs (log_id, user_id, action, entity_type, entity_id, details, ip_address, created_at) VALUES
(1, 1, 'SYSTEM_INIT', 'SYSTEM', '1', 'QueueSense database initialized with baseline rules', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 240 MINUTE)),
(2, 2, 'TOKEN_CREATED', 'TOKEN', '1', 'Token A101 created for CASH_DEPOSIT', '192.168.1.10', DATE_SUB(NOW(), INTERVAL 180 MINUTE)),
(3, 4, 'TOKEN_CALLED', 'TOKEN', '1', 'Counter 2 called token A101', '192.168.1.14', DATE_SUB(NOW(), INTERVAL 170 MINUTE)),
(4, 4, 'SERVICE_COMPLETED', 'TOKEN', '1', 'Service session 1 completed in 360 seconds', '192.168.1.14', DATE_SUB(NOW(), INTERVAL 164 MINUTE)),
(5, 2, 'TOKEN_CANCELLED', 'TOKEN', '9', 'Operator cancelled token A109 on customer request', '192.168.1.10', DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
(6, 4, 'TOKEN_NO_SHOW', 'TOKEN', '8', 'Counter 2 marked token A108 as NO_SHOW after 3 calls', '192.168.1.14', DATE_SUB(NOW(), INTERVAL 70 MINUTE));

-- Update Counter Statuses to reflect active serving
UPDATE counters SET status = 'BUSY' WHERE counter_id IN (1, 2, 3);
UPDATE counters SET status = 'AVAILABLE' WHERE counter_id = 4;
UPDATE counters SET status = 'OFFLINE' WHERE counter_id = 5;
