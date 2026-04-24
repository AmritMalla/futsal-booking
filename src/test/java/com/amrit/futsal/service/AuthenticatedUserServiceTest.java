package com.amrit.futsal.service;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.PaymentRepository;
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
    private FutsalGroundRepository groundRepository;

    private AuthenticatedUserService authenticatedUserService;

    @BeforeEach
    void setUp() {
        authenticatedUserService = new AuthenticatedUserService(
                userRepository,
                bookingRepository,
                paymentRepository,
                groundRepository
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
        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);

        FutsalGround ground = new FutsalGround();
        ground.setId(UUID.randomUUID());
        ground.setCompany(company);

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setUser(bookingUser);
        booking.setGround(ground);
        return booking;
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
