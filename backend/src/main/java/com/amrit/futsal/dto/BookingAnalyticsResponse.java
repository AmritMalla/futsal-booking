package com.amrit.futsal.dto;

import com.amrit.futsal.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingAnalyticsResponse {

    private Long totalBookings;
    private Map<Booking.BookingStatus, Long> bookingsByStatus;
    private Map<String, Long> bookingsByGround;
    private Map<String, Long> bookingsByDay;
    private List<PeakHour> peakHours;
    private BigDecimal averageBookingValue;
    private List<BookingTrend> bookingTrends; // Last 30 days

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PeakHour {
        private String hour;
        private Long count;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookingTrend {
        private String date;
        private Long count;
    }
}
