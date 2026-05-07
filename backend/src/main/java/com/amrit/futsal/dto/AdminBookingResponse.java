package com.amrit.futsal.dto;

import com.amrit.futsal.entity.Booking;
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
public class AdminBookingResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private UUID groundId;
    private String groundName;
    private UUID slotId;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;
    private LocalDateTime bookingDate;
    private Booking.BookingStatus status;

    // Payment information
    private Payment.PaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private String transactionId;

    public static AdminBookingResponse fromEntity(Booking booking, Payment payment) {
        AdminBookingResponse response = new AdminBookingResponse();
        response.setId(booking.getId());
        response.setUserId(booking.getUser().getId());
        response.setUserName(booking.getUser().getName());
        response.setGroundId(booking.getGround().getId());
        response.setGroundName(booking.getGround().getName());
        response.setSlotId(booking.getSlot().getId());
        response.setSlotStartTime(booking.getSlot().getStartTime());
        response.setSlotEndTime(booking.getSlot().getEndTime());
        response.setBookingDate(booking.getBookingDate());
        response.setStatus(booking.getStatus());

        if (payment != null) {
            response.setPaymentStatus(payment.getPaymentStatus());
            response.setPaymentAmount(payment.getAmount());
            response.setTransactionId(payment.getTransactionId());
        }

        return response;
    }
}
