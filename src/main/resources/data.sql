-- 1. Insert Users
INSERT INTO users (user_id, name, email, password, phone_number, role, created_at, updated_at)
VALUES
(1, 'John Doe', 'john.doe@example.com', 'hashed_password_1', '1234567890', 'CUSTOMER', NOW(), NOW()),
(2, 'Jane Smith', 'jane.smith@example.com', 'hashed_password_2', '0987654321', 'FUTSAL_COMPANY', NOW(), NOW());

-- 2. Insert Futsal Companies
INSERT INTO futsal_companies (futsal_id, user_id, company_name, address, description, banner_image_url, rating, created_at, updated_at)
VALUES
(1, 2, 'Star Futsal', '123 Main Street', 'The best futsal ground in the city.', 'https://example.com/banner.jpg', 4.5, NOW(), NOW());

-- 3. Insert Futsal Grounds
INSERT INTO futsal_grounds (ground_id, futsal_id, ground_name, image_url, capacity, created_at, updated_at)
VALUES
(1, 1, 'Main Arena', 'https://example.com/ground.jpg', 20, NOW(), NOW()),
(2, 1, 'Secondary Arena', 'https://example.com/secondary.jpg', 15, NOW(), NOW());

-- 4. Insert Time Slots
INSERT INTO time_slots (slot_id, ground_id, start_time, end_time, price, status, created_at, updated_at)
VALUES
(1, 1, '2024-11-23 10:00:00', '2024-11-23 11:00:00', 50.00, 'AVAILABLE', NOW(), NOW()),
(2, 1, '2024-11-23 11:00:00', '2024-11-23 12:00:00', 50.00, 'BOOKED', NOW(), NOW()),
(3, 2, '2024-11-23 10:00:00', '2024-11-23 11:00:00', 40.00, 'UNAVAILABLE', NOW(), NOW());

-- 5. Insert Bookings
INSERT INTO bookings (booking_id, slot_id, customer_id, payment_status, booking_date, created_at, updated_at)
VALUES
(1, 2, 1, 'COMPLETED', '2024-11-23 09:00:00', NOW(), NOW());

-- 6. Insert Payments
INSERT INTO payments (payment_id, booking_id, payment_method, amount, transaction_id, payment_date, created_at, updated_at)
VALUES
(1, 1, 'CREDIT_CARD', 50.00, 'txn12345', '2024-11-23 09:05:00', NOW(), NOW());

-- 7. Insert Reviews
INSERT INTO reviews (review_id, customer_id, futsal_id, rating, review_text, created_at, updated_at)
VALUES
(1, 1, 1, 5, 'Fantastic experience! Highly recommended.', NOW(), NOW());
