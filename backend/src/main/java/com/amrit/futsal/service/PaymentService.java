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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Payment processPayment(User user, PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (user.getRole() != User.Role.ADMIN && !booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only pay for your own bookings");
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED
                || booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BadRequestException("Payments can only be processed for active bookings");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(booking.getGround().getPricePerHour()) != 0) {
            throw new BadRequestException("Payment amount must match the booking price");
        }

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

        if (payment.getPaymentStatus() != Payment.PaymentStatus.PENDING) {
            throw new BadRequestException("Only pending payments can be updated");
        }

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(payment.getBooking().getGround().getPricePerHour()) != 0) {
                throw new BadRequestException("Payment amount must match the booking price");
            }
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

        if (payment.getPaymentStatus() == status) {
            return payment;
        }
        if (!isAllowedPaymentStatusTransition(payment.getPaymentStatus(), status)) {
            throw new BadRequestException(
                    "Cannot change payment status from " + payment.getPaymentStatus() + " to " + status
            );
        }

        payment.setPaymentStatus(status);

        // Update booking status based on payment status
        Booking booking = payment.getBooking();
        if (status == Payment.PaymentStatus.SUCCESS) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
        } else if (status == Payment.PaymentStatus.FAILED) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.getSlot().setIsBooked(false);
        } else if (status == Payment.PaymentStatus.REFUNDED) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.getSlot().setIsBooked(false);
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

        return updatePaymentStatus(paymentId, Payment.PaymentStatus.REFUNDED);
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean isAllowedPaymentStatusTransition(Payment.PaymentStatus currentStatus,
                                                     Payment.PaymentStatus newStatus) {
        if (currentStatus == Payment.PaymentStatus.PENDING) {
            return newStatus == Payment.PaymentStatus.SUCCESS || newStatus == Payment.PaymentStatus.FAILED;
        }
        return currentStatus == Payment.PaymentStatus.SUCCESS && newStatus == Payment.PaymentStatus.REFUNDED;
    }
}
