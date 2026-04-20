package com.amrit.futsal.service;

import com.amrit.futsal.dto.PaymentRequest;
import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.exception.PaymentException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.PaymentRepository;
import com.amrit.futsal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Payment processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Check if booking already has a successful payment
        List<Payment> existingPayments = paymentRepository.findByBookingId(booking.getId());
        boolean hasSuccessfulPayment = existingPayments.stream()
                .anyMatch(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS);
        if (hasSuccessfulPayment) {
            throw new BadRequestException("This booking already has a successful payment");
        }

        // Create payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(request.getAmount());
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setTransactionId(request.getTransactionId() != null
                ? request.getTransactionId()
                : generateTransactionId());

        // Simulate payment processing (in production, integrate with payment gateway)
        payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        return savedPayment;
    }

    public Optional<Payment> getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    public List<Payment> getPaymentsByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<Payment> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional
    public Payment updatePayment(UUID paymentId, PaymentRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (request.getAmount() != null) {
            payment.setAmount(request.getAmount());
        }

        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }

        return paymentRepository.save(payment);
    }

    public BigDecimal calculateTotalRevenue() {
        BigDecimal total = paymentRepository.sumTotalAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calculateRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findAll().stream()
                .filter(p -> {
                    LocalDateTime bookingDate = p.getBooking().getBookingDate();
                    return !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate) &&
                            p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS;
                })
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Payment updatePaymentStatus(UUID paymentId, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        payment.setPaymentStatus(status);

        // Update booking status based on payment status
        Booking booking = payment.getBooking();
        if (status == Payment.PaymentStatus.SUCCESS) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
        } else if (status == Payment.PaymentStatus.FAILED) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
        }
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getPaymentStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new PaymentException("Only successful payments can be refunded");
        }

        // Simulate refund processing (in production, integrate with payment gateway)
        payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);

        // Cancel the associated booking
        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // Free up the time slot
        booking.getSlot().setIsBooked(false);

        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
