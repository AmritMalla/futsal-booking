Below is a suggested API design for the futsal booking company website. It adheres to RESTful principles and focuses on modularity and clarity. The API is organized by resources, supports CRUD operations, and includes typical endpoints needed for the platform's functionality.

---

## **API Design**

### **Base URL**
`https://api.futsalbooking.com/v1`

---

### **1. User Management**

#### **1.1 Register a User**
- **POST** `/users/register`
- **Description:** Create a new user (customer or futsal company).
- **Request Body:**
    ```json
    {
        "name": "John Doe",
        "email": "johndoe@example.com",
        "password": "securepassword",
        "phone_number": "1234567890",
        "role": "CUSTOMER"
    }
    ```
- **Response:**
    ```json
    {
        "user_id": 1,
        "message": "User registered successfully."
    }
    ```

#### **1.2 Login**
- **POST** `/users/login`
- **Description:** Authenticate a user and issue a JWT token.
- **Request Body:**
    ```json
    {
        "email": "johndoe@example.com",
        "password": "securepassword"
    }
    ```
- **Response:**
    ```json
    {
        "token": "jwt-token-here"
    }
    ```

#### **1.3 Get User Profile**
- **GET** `/users/{userId}`
- **Description:** Retrieve user profile by ID.
- **Response:**
    ```json
    {
        "user_id": 1,
        "name": "John Doe",
        "email": "johndoe@example.com",
        "phone_number": "1234567890",
        "role": "CUSTOMER"
    }
    ```

---

### **2. Futsal Company Management**

#### **2.1 Register a Futsal Company**
- **POST** `/companies`
- **Description:** Register a futsal company (requires the user to have the `FUTSAL_COMPANY` role).
- **Request Body:**
    ```json
    {
        "user_id": 1,
        "company_name": "Star Futsal",
        "address": "123 Main Street",
        "description": "Best futsal ground in town.",
        "banner_image_url": "http://example.com/banner.jpg"
    }
    ```
- **Response:**
    ```json
    {
        "futsal_id": 1,
        "message": "Futsal company registered successfully."
    }
    ```

#### **2.2 Get Futsal Companies**
- **GET** `/companies`
- **Description:** Retrieve a list of all futsal companies.
- **Response:**
    ```json
    [
        {
            "futsal_id": 1,
            "company_name": "Star Futsal",
            "address": "123 Main Street",
            "rating": 4.5,
            "banner_image_url": "http://example.com/banner.jpg"
        }
    ]
    ```

#### **2.3 Get Futsal Company Details**
- **GET** `/companies/{futsalId}`
- **Description:** Retrieve detailed information about a futsal company.
- **Response:**
    ```json
    {
        "futsal_id": 1,
        "company_name": "Star Futsal",
        "address": "123 Main Street",
        "description": "Best futsal ground in town.",
        "rating": 4.5,
        "grounds": [
            {
                "ground_id": 1,
                "ground_name": "Main Arena",
                "capacity": 10,
                "image_url": "http://example.com/ground.jpg"
            }
        ]
    }
    ```

---

### **3. Futsal Grounds Management**

#### **3.1 Add a Futsal Ground**
- **POST** `/grounds`
- **Description:** Add a futsal ground to a company (requires `FUTSAL_COMPANY` role).
- **Request Body:**
    ```json
    {
        "futsal_id": 1,
        "ground_name": "Main Arena",
        "capacity": 10,
        "image_url": "http://example.com/ground.jpg"
    }
    ```
- **Response:**
    ```json
    {
        "ground_id": 1,
        "message": "Futsal ground added successfully."
    }
    ```

#### **3.2 Get Futsal Grounds**
- **GET** `/grounds?futsalId={futsalId}`
- **Description:** Retrieve all grounds for a specific futsal company.
- **Response:**
    ```json
    [
        {
            "ground_id": 1,
            "ground_name": "Main Arena",
            "capacity": 10,
            "image_url": "http://example.com/ground.jpg"
        }
    ]
    ```

---

### **4. Time Slot Management**

#### **4.1 Add Time Slot**
- **POST** `/slots`
- **Description:** Add an available time slot for a futsal ground.
- **Request Body:**
    ```json
    {
        "ground_id": 1,
        "start_time": "2024-11-23T10:00:00",
        "end_time": "2024-11-23T11:00:00",
        "price": 50.0,
        "status": "AVAILABLE"
    }
    ```
- **Response:**
    ```json
    {
        "slot_id": 1,
        "message": "Time slot added successfully."
    }
    ```

#### **4.2 Get Available Slots**
- **GET** `/slots?groundId={groundId}`
- **Description:** Retrieve all available slots for a specific ground.
- **Response:**
    ```json
    [
        {
            "slot_id": 1,
            "start_time": "2024-11-23T10:00:00",
            "end_time": "2024-11-23T11:00:00",
            "price": 50.0,
            "status": "AVAILABLE"
        }
    ]
    ```

---

### **5. Bookings Management**

#### **5.1 Create a Booking**
- **POST** `/bookings`
- **Description:** Book a time slot.
- **Request Body:**
    ```json
    {
        "slot_id": 1,
        "customer_id": 1
    }
    ```
- **Response:**
    ```json
    {
        "booking_id": 1,
        "message": "Booking created successfully."
    }
    ```

#### **5.2 Get Bookings**
- **GET** `/bookings?customerId={customerId}`
- **Description:** Retrieve all bookings for a customer.
- **Response:**
    ```json
    [
        {
            "booking_id": 1,
            "slot_id": 1,
            "ground_name": "Main Arena",
            "booking_date": "2024-11-23T10:00:00",
            "payment_status": "PENDING"
        }
    ]
    ```

---

### **6. Payments Management**

#### **6.1 Process Payment**
- **POST** `/payments`
- **Description:** Handle payment for a booking.
- **Request Body:**
    ```json
    {
        "booking_id": 1,
        "payment_method": "CREDIT_CARD",
        "amount": 50.0,
        "transaction_id": "txn-12345"
    }
    ```
- **Response:**
    ```json
    {
        "payment_id": 1,
        "message": "Payment processed successfully."
    }
    ```

---

### **7. Reviews Management**

#### **7.1 Add a Review**
- **POST** `/reviews`
- **Description:** Submit a review for a futsal company.
- **Request Body:**
    ```json
    {
        "customer_id": 1,
        "futsal_id": 1,
        "rating": 5,
        "review_text": "Amazing experience!"
    }
    ```
- **Response:**
    ```json
    {
        "review_id": 1,
        "message": "Review added successfully."
    }
    ```

#### **7.2 Get Reviews**
- **GET** `/reviews?futsalId={futsalId}`
- **Description:** Retrieve reviews for a specific futsal company.
- **Response:**
    ```json
    [
        {
            "review_id": 1,
            "customer_name": "John Doe",
            "rating": 5,
            "review_text": "Amazing experience!",
            "created_at": "2024-11-22T12:00:00"
        }
    ]
    ```

---

### **Features**
1. **Authentication:** Secure endpoints with JWT.
2. **Pagination:** For lists (`GET /companies`, `GET /grounds`, etc.).
3. **Validation:** Validate request payloads for required fields and formats.
4. **Role-based Authorization:** Enforce role checks (e.g., only `FUTSAL_COMPANY` can create grounds).

Let me know if you'd like any of these endpoints expanded or additional features!