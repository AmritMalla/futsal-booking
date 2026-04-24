package com.amrit.futsal.service;

import com.amrit.futsal.dto.PaymentRequest;
import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentUsesAuthenticatedUserForPaymentRecord() {
        User bookingUser = buildUser(User.Role.USER);
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setUser(bookingUser);

        PaymentRequest request = new PaymentRequest(booking.getId(), BigDecimal.valueOf(1200), "TXN-TEST-1234");

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(booking.getId())).thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.processPayment(bookingUser, request);

        assertEquals(bookingUser, payment.getUser());
        assertEquals(Payment.PaymentStatus.SUCCESS, payment.getPaymentStatus());
        assertEquals(request.getAmount(), payment.getAmount());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(bookingUser, paymentCaptor.getValue().getUser());
    }

    @Test
    void processPaymentRejectsDifferentNonAdminUser() {
        User bookingUser = buildUser(User.Role.USER);
        User anotherUser = buildUser(User.Role.USER);
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setUser(bookingUser);

        PaymentRequest request = new PaymentRequest(booking.getId(), BigDecimal.valueOf(1200), null);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> paymentService.processPayment(anotherUser, request));
    }

    private User buildUser(User.Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setRole(role);
        return user;
    }
}
