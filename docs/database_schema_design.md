# Database Schema Design

## **1. Tables and Relationships**
### **User Table** (`users`)
Stores information for Admins, Owners, and Users.

| Column         | Type        | Constraints                     | Description |
|---------------|------------|---------------------------------|-------------|
| id            | UUID       | PRIMARY KEY                     | Unique user ID |
| name          | VARCHAR(255) | NOT NULL                      | User's full name |
| email         | VARCHAR(255) | UNIQUE, NOT NULL               | User's email (login) |
| password_hash | VARCHAR(255) | NOT NULL                       | Encrypted password |
| role          | ENUM('ADMIN', 'OWNER', 'USER') | NOT NULL | User role |
| phone_number  | VARCHAR(20) | UNIQUE, NULLABLE                | Contact number |
| created_at    | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP       | Registration date |

---

### **Futsal Company Table** (`futsal_companies`)
Each owner can have multiple futsal grounds under a company.

| Column     | Type        | Constraints | Description |
|-----------|------------|-------------|-------------|
| id        | UUID       | PRIMARY KEY | Unique company ID |
| owner_id  | UUID       | FOREIGN KEY (users.id) | Owner who manages the company |
| name      | VARCHAR(255) | NOT NULL, UNIQUE | Name of the futsal company |
| location  | TEXT       | NOT NULL | Address/location details |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration date |

---

### **Futsal Ground Table** (`futsal_grounds`)
Represents individual futsal grounds belonging to a company.

| Column       | Type        | Constraints | Description |
|-------------|------------|-------------|-------------|
| id          | UUID       | PRIMARY KEY | Unique ground ID |
| company_id  | UUID       | FOREIGN KEY (futsal_companies.id) | Company managing this ground |
| name        | VARCHAR(255) | NOT NULL, UNIQUE | Name of the futsal ground |
| surface_type | VARCHAR(50) | CHECK (surface_type IN ('Turf', 'Concrete', 'Grass')) | Type of surface |
| price_per_hour | DECIMAL(10,2) | NOT NULL | Hourly rental price |
| image_url   | TEXT        | NULLABLE | Ground photo storage URL |
| created_at  | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP | Creation date |

---

### **Time Slot Table** (`time_slots`)
Stores available slots for booking.

| Column      | Type      | Constraints | Description |
|------------|----------|-------------|-------------|
| id         | UUID     | PRIMARY KEY | Unique slot ID |
| ground_id  | UUID     | FOREIGN KEY (futsal_grounds.id) | Associated ground |
| start_time | TIMESTAMP | NOT NULL | Start of the slot |
| end_time   | TIMESTAMP | NOT NULL | End of the slot |
| is_booked  | BOOLEAN  | DEFAULT FALSE | Indicates if the slot is booked |

---

### **Booking Table** (`bookings`)
Stores user bookings.

| Column       | Type      | Constraints | Description |
|-------------|----------|-------------|-------------|
| id          | UUID     | PRIMARY KEY | Unique booking ID |
| user_id     | UUID     | FOREIGN KEY (users.id) | User who booked |
| ground_id   | UUID     | FOREIGN KEY (futsal_grounds.id) | Futsal ground |
| slot_id     | UUID     | FOREIGN KEY (time_slots.id) | Booked slot |
| booking_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Booking timestamp |
| status      | ENUM('CONFIRMED', 'CANCELLED', 'COMPLETED') | NOT NULL | Status of booking |

---

### **Payment Table** (`payments`)
Stores payments made by users.

| Column      | Type        | Constraints | Description |
|------------|------------|-------------|-------------|
| id         | UUID       | PRIMARY KEY | Unique payment ID |
| booking_id | UUID       | FOREIGN KEY (bookings.id) | Associated booking |
| user_id    | UUID       | FOREIGN KEY (users.id) | User who paid |
| amount     | DECIMAL(10,2) | NOT NULL | Amount paid |
| payment_status | ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') | NOT NULL | Payment status |
| transaction_id | VARCHAR(255) | UNIQUE, NULLABLE | External payment gateway transaction ID |

---

### **Review & Ratings Table** (`reviews`)
Stores user feedback.

| Column      | Type        | Constraints | Description |
|------------|------------|-------------|-------------|
| id         | UUID       | PRIMARY KEY | Unique review ID |
| user_id    | UUID       | FOREIGN KEY (users.id) | User who gave feedback |
| ground_id  | UUID       | FOREIGN KEY (futsal_grounds.id) | Futsal ground |
| rating     | INT        | CHECK (rating BETWEEN 1 AND 5) | Rating (1-5) |
| review_text | TEXT      | NULLABLE | Optional review comments |

---

### **Reports Table** (`reports`)
Stores generated reports for owners and admins.

| Column      | Type        | Constraints | Description |
|------------|------------|-------------|-------------|
| id         | UUID       | PRIMARY KEY | Unique report ID |
| owner_id   | UUID       | FOREIGN KEY (users.id) | Owner receiving report |
| report_type | ENUM('REVENUE', 'BOOKINGS', 'CUSTOMERS') | NOT NULL | Type of report |
| generated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Report generation date |

---

## **Relationships**
- **Users** can be **Admins, Owners, or Customers**.
- **Owners** manage **Futsal Companies**, which contain **Futsal Grounds**.
- **Each ground** has multiple **Time Slots**, which users can **book**.
- **Users** make **Payments** for their **Bookings**.
- **Users** can submit **Reviews** for **Grounds**.
- **Owners and Admins** can generate **Reports**.
