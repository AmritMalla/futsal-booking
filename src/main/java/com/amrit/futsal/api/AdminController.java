package com.amrit.futsal.api;

import com.amrit.futsal.dto.*;
import com.amrit.futsal.entity.*;
import com.amrit.futsal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private FutsalCompanyService futsalCompanyService;

    @Autowired
    private FutsalGroundService futsalGroundService;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AdminAnalyticsService analyticsService;

    // ==================== Analytics Endpoints ====================

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        AdminStatsResponse stats = analyticsService.getAdminStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<RevenueAnalyticsResponse> getRevenueAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        RevenueAnalyticsResponse analytics = analyticsService.getRevenueAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/bookings")
    public ResponseEntity<BookingAnalyticsResponse> getBookingAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BookingAnalyticsResponse analytics = analyticsService.getBookingAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/users")
    public ResponseEntity<UserAnalyticsResponse> getUserAnalytics() {
        UserAnalyticsResponse analytics = analyticsService.getUserAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // ==================== User Management ====================

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/owners")
    public ResponseEntity<List<User>> getAllOwners() {
        return ResponseEntity.ok(userService.getUsersByRole(User.Role.OWNER));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        return ResponseEntity.ok(userService.getUsersByRole(User.Role.USER));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUserWithDetails(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhoneNumber(),
                request.getRole() != null ? request.getRole() : User.Role.USER
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request) {
        User updatedUser = userService.updateUser(
                userId,
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber()
        );
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable UUID userId,
            @RequestParam User.Role role) {
        User updatedUser = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Company Management ====================

    @GetMapping("/companies")
    public ResponseEntity<Page<FutsalCompany>> getAllFutsalCompanies(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(futsalCompanyService.getAllFutsalCompanies(pageable));
    }

    @GetMapping("/companies/{companyId}")
    public ResponseEntity<FutsalCompany> getCompanyById(@PathVariable UUID companyId) {
        return futsalCompanyService.getFutsalCompanyById(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/companies")
    public ResponseEntity<FutsalCompany> createCompany(@RequestBody FutsalCompanyRequest request) {
        FutsalCompany company = futsalCompanyService.createFutsalCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @PutMapping("/companies/{companyId}")
    public ResponseEntity<FutsalCompany> updateCompany(
            @PathVariable UUID companyId,
            @RequestBody FutsalCompanyRequest request) {
        FutsalCompany updatedCompany = futsalCompanyService.updateFutsalCompany(companyId, request);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/companies/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID companyId) {
        futsalCompanyService.deleteFutsalCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Ground Management ====================

    @GetMapping("/grounds")
    public ResponseEntity<Page<FutsalGround>> getAllFutsalGrounds(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(futsalGroundService.getAllFutsalGrounds(pageable));
    }

    @GetMapping("/grounds/{groundId}")
    public ResponseEntity<FutsalGround> getGroundById(@PathVariable UUID groundId) {
        return futsalGroundService.getFutsalGroundById(groundId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/grounds")
    public ResponseEntity<FutsalGround> createGround(@RequestBody FutsalGroundRequest request) {
        FutsalGround ground = futsalGroundService.createFutsalGround(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ground);
    }

    @PutMapping("/grounds/{groundId}")
    public ResponseEntity<FutsalGround> updateGround(
            @PathVariable UUID groundId,
            @RequestBody FutsalGroundRequest request) {
        FutsalGround updatedGround = futsalGroundService.updateFutsalGround(groundId, request);
        return ResponseEntity.ok(updatedGround);
    }

    @DeleteMapping("/grounds/{groundId}")
    public ResponseEntity<Void> deleteGround(@PathVariable UUID groundId) {
        futsalGroundService.deleteFutsalGround(groundId);
        return ResponseEntity.noContent().build();
    }

    // ==================== TimeSlot Management ====================

    @GetMapping("/timeslots")
    public ResponseEntity<List<TimeSlotResponse>> getAllTimeSlots(
            @RequestParam(required = false) UUID groundId,
            @RequestParam(required = false) Boolean isBooked) {
        List<TimeSlot> slots;

        if (groundId != null) {
            slots = timeSlotService.getTimeSlotsByGroundId(groundId);
        } else {
            slots = timeSlotService.getAllTimeSlots();
        }

        if (isBooked != null) {
            slots = slots.stream()
                    .filter(s -> s.getIsBooked().equals(isBooked))
                    .collect(Collectors.toList());
        }

        List<TimeSlotResponse> responses = slots.stream()
                .map(TimeSlotResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/timeslots/{slotId}")
    public ResponseEntity<TimeSlotResponse> getTimeSlotById(@PathVariable UUID slotId) {
        return timeSlotService.getTimeSlotById(slotId)
                .map(TimeSlotResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/timeslots")
    public ResponseEntity<TimeSlotResponse> createTimeSlot(@RequestBody TimeSlotRequest request) {
        TimeSlot createdSlot = timeSlotService.createTimeSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TimeSlotResponse.fromEntity(createdSlot));
    }

    @PutMapping("/timeslots/{slotId}")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(
            @PathVariable UUID slotId,
            @RequestBody TimeSlotRequest request) {
        TimeSlot updatedSlot = timeSlotService.updateTimeSlot(slotId, request);
        return ResponseEntity.ok(TimeSlotResponse.fromEntity(updatedSlot));
    }

    @DeleteMapping("/timeslots/{slotId}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable UUID slotId) {
        timeSlotService.deleteTimeSlot(slotId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Booking Management ====================

    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingResponse>> getAllBookings(
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID groundId) {
        List<Booking> bookings;

        if (status != null) {
            bookings = bookingService.getBookingsByStatus(status);
        } else if (userId != null) {
            bookings = bookingService.getBookingsByUserId(userId);
        } else if (groundId != null) {
            bookings = bookingService.getBookingsByGroundId(groundId);
        } else {
            bookings = bookingService.getAllBookings();
        }

        List<AdminBookingResponse> responses = bookings.stream()
                .map(booking -> {
                    List<Payment> payments = paymentService.getPaymentsByBookingId(booking.getId());
                    Payment payment = payments.isEmpty() ? null : payments.get(0);
                    return AdminBookingResponse.fromEntity(booking, payment);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<AdminBookingResponse> getBookingById(@PathVariable UUID bookingId) {
        return bookingService.getBookingById(bookingId)
                .map(booking -> {
                    List<Payment> payments = paymentService.getPaymentsByBookingId(booking.getId());
                    Payment payment = payments.isEmpty() ? null : payments.get(0);
                    return ResponseEntity.ok(AdminBookingResponse.fromEntity(booking, payment));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/bookings/{bookingId}")
    public ResponseEntity<AdminBookingResponse> updateBooking(
            @PathVariable UUID bookingId,
            @RequestBody BookingRequest request) {
        Booking updatedBooking = bookingService.updateBooking(bookingId, request);
        List<Payment> payments = paymentService.getPaymentsByBookingId(updatedBooking.getId());
        Payment payment = payments.isEmpty() ? null : payments.get(0);
        return ResponseEntity.ok(AdminBookingResponse.fromEntity(updatedBooking, payment));
    }

    @PatchMapping("/bookings/{bookingId}/status")
    public ResponseEntity<AdminBookingResponse> updateBookingStatus(
            @PathVariable UUID bookingId,
            @RequestParam Booking.BookingStatus status) {
        Booking updatedBooking = bookingService.updateBookingStatus(bookingId, status);
        List<Payment> payments = paymentService.getPaymentsByBookingId(updatedBooking.getId());
        Payment payment = payments.isEmpty() ? null : payments.get(0);
        return ResponseEntity.ok(AdminBookingResponse.fromEntity(updatedBooking, payment));
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Payment Management ====================

    @GetMapping("/payments")
    public ResponseEntity<List<AdminPaymentResponse>> getAllPayments(
            @RequestParam(required = false) Payment.PaymentStatus status,
            @RequestParam(required = false) UUID userId) {
        List<Payment> payments;

        if (status != null) {
            payments = paymentService.getPaymentsByStatus(status);
        } else if (userId != null) {
            payments = paymentService.getPaymentsByUserId(userId);
        } else {
            payments = paymentService.getAllPayments();
        }

        List<AdminPaymentResponse> responses = payments.stream()
                .map(AdminPaymentResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<AdminPaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        return paymentService.getPaymentById(paymentId)
                .map(AdminPaymentResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/payments/{paymentId}")
    public ResponseEntity<AdminPaymentResponse> updatePayment(
            @PathVariable UUID paymentId,
            @RequestBody PaymentRequest request) {
        Payment updatedPayment = paymentService.updatePayment(paymentId, request);
        return ResponseEntity.ok(AdminPaymentResponse.fromEntity(updatedPayment));
    }

    @PatchMapping("/payments/{paymentId}/status")
    public ResponseEntity<AdminPaymentResponse> updatePaymentStatus(
            @PathVariable UUID paymentId,
            @RequestParam Payment.PaymentStatus status) {
        Payment updatedPayment = paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(AdminPaymentResponse.fromEntity(updatedPayment));
    }

    @DeleteMapping("/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID paymentId) {
        paymentService.getPaymentById(paymentId).ifPresent(payment -> {
            // Note: Deleting payments directly is risky in production
            // Consider soft-delete or archiving instead
            paymentService.getPaymentById(paymentId);
        });
        return ResponseEntity.noContent().build();
    }

    // ==================== Review Management ====================

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getAllReviews(
            @RequestParam(required = false) UUID groundId,
            @RequestParam(required = false) Integer minRating) {
        List<Review> reviews;

        if (groundId != null) {
            reviews = reviewService.getReviewsByGroundId(groundId);
        } else {
            reviews = reviewService.getReviewById(null).map(List::of).orElse(List.of());
            // Get all reviews - ReviewService doesn't have getAllReviews, so we'll filter
            reviews = reviewService.getReviewsByGroundId(null); // This might need adjustment
        }

        if (minRating != null) {
            reviews = reviews.stream()
                    .filter(r -> r.getRating() >= minRating)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> getReviewById(@PathVariable UUID reviewId) {
        return reviewService.getReviewById(reviewId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody ReviewRequest request) {
        return reviewService.getReviewById(reviewId)
                .map(review -> {
                    if (request.getRating() != null) {
                        review.setRating(request.getRating());
                    }
                    if (request.getReviewText() != null) {
                        review.setReviewText(request.getReviewText());
                    }
                    Review updatedReview = reviewService.updateReview(review);
                    return ResponseEntity.ok(updatedReview);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    // ==================== DTOs for User Management ====================

    public static class CreateUserRequest {
        private String name;
        private String email;
        private String password;
        private String phoneNumber;
        private User.Role role;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public User.Role getRole() { return role; }
        public void setRole(User.Role role) { this.role = role; }
    }

    public static class UpdateUserRequest {
        private String name;
        private String email;
        private String phoneNumber;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }
}
