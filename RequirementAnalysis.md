
Futsal Project Design 
---

### **Entities and Relationships**

#### 1. **Users**
- Stores information about both futsal companies and customers (single table with roles).
- **Columns**:
    - `user_id` (PK, auto-incremented)
    - `name`
    - `email` (unique)
    - `password` (hashed)
    - `phone_number`
    - `role` (ENUM: `futsal_company`, `customer`)
    - `created_at`, `updated_at`

#### 2. **Futsal Companies**
- Contains futsal-specific information.
- **Columns**:
    - `futsal_id` (PK, auto-incremented)
    - `user_id` (FK, links to `Users.user_id`)
    - `company_name`
    - `address`
    - `description`
    - `banner_image_url`
    - `rating` (calculated based on customer reviews)
    - `created_at`, `updated_at`

#### 3. **Futsal Grounds**
- Represents individual futsal grounds for a company.
- **Columns**:
    - `ground_id` (PK, auto-incremented)
    - `futsal_id` (FK, links to `Futsal_Companies.futsal_id`)
    - `ground_name`
    - `image_url`
    - `capacity` (optional)
    - `created_at`, `updated_at`

#### 4. **Time Slots**
- Defines the available time slots for each futsal ground.
- **Columns**:
    - `slot_id` (PK, auto-incremented)
    - `ground_id` (FK, links to `Futsal_Grounds.ground_id`)
    - `start_time` (DATETIME)
    - `end_time` (DATETIME)
    - `price`
    - `status` (ENUM: `available`, `booked`, `unavailable`)
    - `created_at`, `updated_at`

#### 5. **Bookings**
- Tracks bookings made by customers.
- **Columns**:
    - `booking_id` (PK, auto-incremented)
    - `slot_id` (FK, links to `Time_Slots.slot_id`)
    - `customer_id` (FK, links to `Users.user_id`)
    - `payment_status` (ENUM: `pending`, `completed`, `failed`)
    - `booking_date` (DATETIME)
    - `created_at`, `updated_at`

#### 6. **Payments**
- Handles payment details for bookings.
- **Columns**:
    - `payment_id` (PK, auto-incremented)
    - `booking_id` (FK, links to `Bookings.booking_id`)
    - `payment_method` (ENUM: `credit_card`, `bank_transfer`, `paypal`)
    - `amount`
    - `transaction_id` (unique, external payment reference)
    - `payment_date` (DATETIME)
    - `created_at`, `updated_at`

#### 7. **Reviews**
- Allows customers to leave feedback on futsal companies.
- **Columns**:
    - `review_id` (PK, auto-incremented)
    - `customer_id` (FK, links to `Users.user_id`)
    - `futsal_id` (FK, links to `Futsal_Companies.futsal_id`)
    - `rating` (1-5 scale)
    - `review_text`
    - `created_at`, `updated_at`

---

### **Additional Features**

1. **Multi-Tenant Separation**:
    - Every `futsal_company` is scoped by its unique `user_id`, ensuring no cross-data leakage.
    - Shared tables (`Users`, `Payments`) with role-based access for scalability.

2. **Search and Filter**:
    - You can implement search filters using `address`, `price`, and `rating` to make it user-friendly for customers.

3. **Indexes**:
    - Create indexes on frequently queried columns, such as `email`, `futsal_id`, and `ground_id`, for faster lookups.

4. **Audit Trails**:
    - Use `created_at` and `updated_at` timestamps to track modifications for all records.

---

### **ER Diagram**
This structure will have relationships such as:
- `Users` (1-to-1) → `Futsal Companies`
- `Futsal Companies` (1-to-many) → `Futsal Grounds`
- `Futsal Grounds` (1-to-many) → `Time Slots`
- `Time Slots` (1-to-1) → `Bookings`
- `Bookings` (1-to-1) → `Payments`
- `Futsal Companies` (1-to-many) ← `Reviews`

---

### SQL Table Creation Scripts (Snippet)

Here’s an example for a couple of tables:

```sql
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15),
    role ENUM('futsal_company', 'customer') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE Futsal_Companies (
    futsal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    address TEXT,
    description TEXT,
    banner_image_url TEXT,
    rating FLOAT DEFAULT 0.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE Futsal_Grounds (
    ground_id INT AUTO_INCREMENT PRIMARY KEY,
    futsal_id INT NOT NULL,
    ground_name VARCHAR(255) NOT NULL,
    image_url TEXT,
    capacity INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (futsal_id) REFERENCES Futsal_Companies(futsal_id)
);
```

---

### Scaling Considerations
1. **Horizontal Scalability**:
    - Use database sharding to separate tenants if your data grows substantially.
2. **Caching**:
    - Use caching (e.g., Redis) for frequently accessed data like available slots.
3. **Security**:
    - Encrypt sensitive data and use parameterized queries to avoid SQL injection.

This schema provides flexibility, scalability, and maintainability for your futsal booking platform. 

