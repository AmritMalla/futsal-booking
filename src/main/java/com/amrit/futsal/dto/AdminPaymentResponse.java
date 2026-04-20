package com.amrit.futsal.dto;

import com.amrit.futsal.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPaymentResponse {

    private UUID id;
    private UUID bookingId;
    private UUID userId;
    private String userName;
    private BigDecimal amount;
    private Payment.PaymentStatus paymentStatus;
    private String transactionId;

    // Booking details
    private String groundName;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;

    public static AdminPaymentResponse fromEntity(Payment payment) {
        AdminPaymentResponse response = new AdminPaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBooking().getId());
        response.setUserId(payment.getUser().getId());
        response.setUserName(payment.getUser().getName());
        response.setAmount(payment.getAmount());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setTransactionId(payment.getTransactionId());

        // Add booking details
        response.setGroundName(payment.getBooking().getGround().getName());
        response.setSlotStartTime(payment.getBooking().getSlot().getStartTime());
        response.setSlotEndTime(payment.getBooking().getSlot().getEndTime());

        return response;
    }
}
