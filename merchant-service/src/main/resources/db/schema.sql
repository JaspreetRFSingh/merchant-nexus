-- Database schema for Merchant Service
-- Demonstrates: DB schema design, indexing, constraints

CREATE DATABASE IF NOT EXISTS merchant_db;
USE merchant_db;

CREATE USER IF NOT EXISTS 'merchant_user'@'localhost' IDENTIFIED BY 'merchant_pass';
GRANT ALL PRIVILEGES ON merchant_db.* TO 'merchant_user'@'localhost';
FLUSH PRIVILEGES;

-- Merchants table
CREATE TABLE IF NOT EXISTS merchants (
    id VARCHAR(36) PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    business_registration_number VARCHAR(50) NOT NULL UNIQUE,
    owner_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_email (email),
    INDEX idx_business_name (business_name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data for testing
INSERT INTO merchants (id, business_name, business_registration_number, owner_name, email, phone, status, street, city, state, postal_code, country) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Seoul Kimchi House', '123-45-67890', 'Kim Min-su', 'kim@seoulkimchi.kr', '+82-10-1234-5678', 'ACTIVE', '123 Gangnam-daero', 'Seoul', 'Gangnam-gu', '06000', 'South Korea'),
('550e8400-e29b-41d4-a716-446655440002', 'Busan Seafood Co', '234-56-78901', 'Park Ji-young', 'park@busanseafood.kr', '+82-10-2345-6789', 'ACTIVE', '456 Haeundae-ro', 'Busan', 'Haeundae-gu', '48000', 'South Korea'),
('550e8400-e29b-41d4-a716-446655440003', 'Jeju Organic Farm', '345-67-89012', 'Lee Sang-ho', 'lee@jejuorganic.kr', '+82-10-3456-7890', 'PENDING_VERIFICATION', '789 Jungmun-ro', 'Jeju', 'Seogwipo', '63561', 'South Korea'),
('550e8400-e29b-41d4-a716-446655440004', 'Incheon Trading', '456-78-90123', 'Choi Soo-jin', 'choi@incheontrading.kr', '+82-10-4567-8901', 'SUSPENDED', '321 Songdo-dong', 'Incheon', 'Yeonsu-gu', '21990', 'South Korea'),
('550e8400-e29b-41d4-a716-446655440005', 'Daegu Textiles', '567-89-01234', 'Jung Hyun-woo', 'jung@daegutextiles.kr', '+82-10-5678-9012', 'ACTIVE', '654 Dongseong-ro', 'Daegu', 'Jung-gu', '41911', 'South Korea');
