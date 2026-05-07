package com.amrit.futsal.dto;

import com.amrit.futsal.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private UUID id;
    private UUID bookingId;
    private UUID userId;
    private String userName;
    private BigDecimal amount;
    private Payment.PaymentStatus paymentStatus;
    private String transactionId;

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBooking().getId());
        response.setUserId(payment.getUser().getId());
        response.setUserName(payment.getUser().getName());
        response.setAmount(payment.getAmount());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setTransactionId(payment.getTransactionId());
        return response;
    }
}
