-- Database schema for Settlement Service
-- Demonstrates: Financial data modeling, audit trails, optimistic locking

CREATE DATABASE IF NOT EXISTS settlement_db;
USE settlement_db;

CREATE USER IF NOT EXISTS 'settlement_user'@'localhost' IDENTIFIED BY 'settlement_pass';
GRANT ALL PRIVILEGES ON settlement_db.* TO 'settlement_user'@'localhost';
FLUSH PRIVILEGES;

-- Settlements table with optimistic locking
CREATE TABLE IF NOT EXISTS settlements (
    id VARCHAR(36) PRIMARY KEY,
    merchant_id VARCHAR(36) NOT NULL,
    settlement_period_start DATE NOT NULL,
    settlement_period_end DATE NOT NULL,
    gross_amount DECIMAL(15,2) NOT NULL,
    commission DECIMAL(15,2) NOT NULL,
    tax DECIMAL(15,2) NOT NULL,
    adjustment DECIMAL(15,2) DEFAULT 0.00,
    net_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    bank_account_number VARCHAR(50) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    payment_reference VARCHAR(100),
    rejection_reason VARCHAR(500),
    processed_by VARCHAR(100),
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    version BIGINT DEFAULT 0,
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status),
    INDEX idx_period (settlement_period_start, settlement_period_end),
    INDEX idx_requested_at (requested_at),
    UNIQUE KEY unique_merchant_period (merchant_id, settlement_period_start, settlement_period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Settlement audit log for compliance
CREATE TABLE IF NOT EXISTS settlement_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_id VARCHAR(36) NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    performed_by VARCHAR(100),
    reason VARCHAR(500),
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_settlement_id (settlement_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data for testing
INSERT INTO settlements (id, merchant_id, settlement_period_start, settlement_period_end, gross_amount, commission, tax, adjustment, net_amount, status, bank_account_number, bank_name, requested_at) VALUES
('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '2024-03-01', '2024-03-15', 15000000.00, 450000.00, 45000.00, 0.00, 14505000.00, 'COMPLETED', '1234567890123', 'KB Kookmin Bank', '2024-03-16 10:00:00'),
('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', '2024-03-16', '2024-03-31', 18000000.00, 540000.00, 54000.00, 0.00, 17406000.00, 'PROCESSING', '1234567890123', 'KB Kookmin Bank', '2024-04-01 10:00:00'),
('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', '2024-03-01', '2024-03-31', 25000000.00, 750000.00, 75000.00, 0.00, 24175000.00, 'PENDING', '2345678901234', 'Shinhan Bank', '2024-04-01 14:00:00'),
('660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440005', '2024-03-01', '2024-03-31', 8000000.00, 240000.00, 24000.00, 0.00, 7736000.00, 'PENDING', '3456789012345', 'Woori Bank', '2024-04-01 16:00:00');
