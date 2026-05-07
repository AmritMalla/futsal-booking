package com.amrit.futsal.service;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.Review;
import com.amrit.futsal.entity.Report;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.PaymentRepository;
import com.amrit.futsal.repository.ReportRepository;
import com.amrit.futsal.repository.ReviewRepository;
import com.amrit.futsal.repository.TimeSlotRepository;
import com.amrit.futsal.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FutsalCompanyRepository companyRepository;

    @Mock
    private FutsalGroundRepository groundRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private AuthenticatedUserService authenticatedUserService;

    @BeforeEach
    void setUp() {
        authenticatedUserService = new AuthenticatedUserService(
                userRepository,
                bookingRepository,
                paymentRepository,
                companyRepository,
                groundRepository,
                timeSlotRepository,
                reportRepository,
                reviewRepository
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireCurrentUserOrAdminAllowsMatchingUser() {
        User currentUser = buildUser(User.Role.USER);
        authenticateAs(currentUser);
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));

        assertDoesNotThrow(() -> authenticatedUserService.requireCurrentUserOrAdmin(currentUser.getId()));
    }

    @Test
    void requireCurrentUserOrAdminRejectsDifferentUser() {
        User currentUser = buildUser(User.Role.USER);
        authenticateAs(currentUser);
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));

        assertThrows(
                AccessDeniedException.class,
                () -> authenticatedUserService.requireCurrentUserOrAdmin(UUID.randomUUID())
        );
    }

    @Test
    void requireBookingAccessAllowsGroundOwner() {
        User owner = buildUser(User.Role.OWNER);
        User bookingUser = buildUser(User.Role.USER);
        Booking booking = buildBooking(bookingUser, owner);

        authenticateAs(owner);
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertDoesNotThrow(() -> authenticatedUserService.requireBookingAccess(booking.getId()));
    }

    @Test
    void requireCompanyOwnerOrAdminRejectsDifferentOwner() {
        User owner = buildUser(User.Role.OWNER);
        User anotherOwner = buildUser(User.Role.OWNER);
        FutsalCompany company = new FutsalCompany();
        company.setId(UUID.randomUUID());
        company.setOwner(owner);

        authenticateAs(anotherOwner);
        when(userRepository.findByEmail(anotherOwner.getEmail())).thenReturn(Optional.of(anotherOwner));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        assertThrows(
                AccessDeniedException.class,
                () -> authenticatedUserService.requireCompanyOwnerOrAdmin(company.getId())
        );
    }

    @Test
    void requireGroundOwnerOrAdminRejectsDifferentOwner() {
        User owner = buildUser(User.Role.OWNER);
        User anotherOwner = buildUser(User.Role.OWNER);
        FutsalGround ground = buildGround(owner);

        authenticateAs(anotherOwner);
        when(userRepository.findByEmail(anotherOwner.getEmail())).thenReturn(Optional.of(anotherOwner));
        when(groundRepository.findById(ground.getId())).thenReturn(Optional.of(ground));

        assertThrows(
                AccessDeniedException.class,
                () -> authenticatedUserService.requireGroundOwnerOrAdmin(ground.getId())
        );
    }

    @Test
    void requirePaymentAccessRejectsUnrelatedUser() {
        User unrelatedUser = buildUser(User.Role.USER);
        User bookingUser = buildUser(User.Role.USER);
        User owner = buildUser(User.Role.OWNER);
        Payment payment = buildPayment(bookingUser, owner);

        authenticateAs(unrelatedUser);
        when(userRepository.findByEmail(unrelatedUser.getEmail())).thenReturn(Optional.of(unrelatedUser));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThrows(
                AccessDeniedException.class,
                () -> authenticatedUserService.requirePaymentAccess(payment.getId())
        );
    }

    @Test
    void requireReviewOwnerOrAdminRejectsDifferentUser() {
        User owner = buildUser(User.Role.USER);
        User anotherUser = buildUser(User.Role.USER);
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setUser(owner);

        authenticateAs(anotherUser);
        when(userRepository.findByEmail(anotherUser.getEmail())).thenReturn(Optional.of(anotherUser));
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        assertThrows(
                AccessDeniedException.class,
                () -> authenticatedUserService.requireReviewOwnerOrAdmin(review.getId())
        );
    }

    @Test
    void requireTimeSlotOwnerOrAdminAllowsGroundOwner() {
        User owner = buildUser(User.Role.OWNER);
        FutsalGround ground = buildGround(owner);
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(UUID.randomUUID());
        timeSlot.setGround(ground);

        authenticateAs(owner);
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(timeSlotRepository.findById(timeSlot.getId())).thenReturn(Optional.of(timeSlot));

        assertDoesNotThrow(() -> authenticatedUserService.requireTimeSlotOwnerOrAdmin(timeSlot.getId()));
    }

    @Test
    void requireReportOwnerOrAdminRejectsDifferentOwner() {
        User owner = buildUser(User.Role.OWNER);
        User anotherOwner = buildUser(User.Role.OWNER);
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setOwner(owner);

        authenticateAs(anotherOwner);
        when(userRepository.findByEmail(anotherOwner.getEmail())).thenReturn(Optional.of(anotherOwner));
        when(reportRepository.findById(report.getId())).thenReturn(Optional.of(report));

        assertThrows(
                AccessDeniedException.class,
                () -> authenticatedUserService.requireReportOwnerOrAdmin(report.getId())
        );
    }

    private void authenticateAs(User user) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "password",
                List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    private User buildUser(User.Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setName("Test User");
        user.setRole(role);
        return user;
    }

    private Booking buildBooking(User bookingUser, User owner) {
        FutsalGround ground = buildGround(owner);

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setUser(bookingUser);
        booking.setGround(ground);
        return booking;
    }

    private FutsalGround buildGround(User owner) {
        FutsalCompany company = new FutsalCompany();
        company.setId(UUID.randomUUID());
        company.setOwner(owner);

        FutsalGround ground = new FutsalGround();
        ground.setId(UUID.randomUUID());
        ground.setCompany(company);
        return ground;
    }

    private Payment buildPayment(User bookingUser, User owner) {
        Booking booking = buildBooking(bookingUser, owner);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setUser(bookingUser);
        payment.setBooking(booking);
        return payment;
    }
}
