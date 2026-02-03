-- 1. Insert Users (id, name, email, password_hash, phone_number, role, created_at)
INSERT INTO users (id, name, email, password_hash, phone_number, role, created_at)
VALUES
('a0000000-0000-0000-0000-000000000001', 'John Doe', 'john.doe@example.com', 'hashed_password_1', '1234567890', 'USER', NOW()),
('a0000000-0000-0000-0000-000000000002', 'Jane Smith', 'jane.smith@example.com', 'hashed_password_2', '0987654321', 'OWNER', NOW());

-- 2. Insert Futsal Companies (id, owner_id, name, location, created_at)
INSERT INTO futsal_companies (id, owner_id, name, location, created_at)
VALUES
('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002', 'Star Futsal', '123 Main Street', NOW());

-- 3. Insert Futsal Grounds (id, company_id, name, surface_type, price_per_hour, image_url, created_at)
INSERT INTO futsal_grounds (id, company_id, name, surface_type, price_per_hour, image_url, created_at)
VALUES
('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'Main Arena', 'TURF', 50.00, 'https://example.com/ground.jpg', NOW()),
('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'Secondary Arena', 'CONCRETE', 40.00, 'https://example.com/secondary.jpg', NOW());

-- 4. Insert Time Slots (id, ground_id, start_time, end_time, is_booked, version)
INSERT INTO time_slots (id, ground_id, start_time, end_time, is_booked, version)
VALUES
('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', '2024-11-23 10:00:00', '2024-11-23 11:00:00', false, 0),
('d0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', '2024-11-23 11:00:00', '2024-11-23 12:00:00', true, 0),
('d0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000002', '2024-11-23 10:00:00', '2024-11-23 11:00:00', false, 0);

-- 5. Insert Bookings (id, user_id, ground_id, slot_id, booking_date, status)
INSERT INTO bookings (id, user_id, ground_id, slot_id, booking_date, status)
VALUES
('e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000002', '2024-11-23 09:00:00', 'COMPLETED');

-- 6. Insert Payments (id, booking_id, user_id, amount, payment_status, transaction_id)
INSERT INTO payments (id, booking_id, user_id, amount, payment_status, transaction_id)
VALUES
('f0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 50.00, 'SUCCESS', 'txn12345');

-- 7. Insert Reviews (id, user_id, ground_id, rating, review_text, created_at)
INSERT INTO reviews (id, user_id, ground_id, rating, review_text, created_at)
VALUES
('90000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 5, 'Fantastic experience! Highly recommended.', NOW());
