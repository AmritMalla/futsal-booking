package com.amrit.futsal.dto;

import com.amrit.futsal.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

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

    public static BookingResponse fromEntity(Booking booking) {
        BookingResponse response = new BookingResponse();
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
        return response;
    }
}
