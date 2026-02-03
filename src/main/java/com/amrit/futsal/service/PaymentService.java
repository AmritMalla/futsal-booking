package com.amrit.futsal.service;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    @Transactional
    public Payment processPayment(Payment payment) {
        // Update booking status when payment is successful
        if (payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
            bookingService.updateBookingStatus(payment.getBooking().getId(), Booking.BookingStatus.CONFIRMED);
        }
        return paymentRepository.save(payment);
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
    
    public Payment updatePaymentStatus(UUID paymentId, Payment.PaymentStatus status) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setPaymentStatus(status);
            
            // Update booking status based on payment status
            if (status == Payment.PaymentStatus.SUCCESS) {
                bookingService.updateBookingStatus(payment.getBooking().getId(), Booking.BookingStatus.CONFIRMED);
            } else if (status == Payment.PaymentStatus.FAILED) {
                bookingService.updateBookingStatus(payment.getBooking().getId(), Booking.BookingStatus.CANCELLED);
            }
            
            return paymentRepository.save(payment);
        }
        return null;
    }
}
