package com.amrit.futsal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class ReportData {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueReport {
        private BigDecimal totalRevenue;
        private int totalBookings;
        private BigDecimal averageBookingValue;
        private Map<String, BigDecimal> revenueByGround;
        private List<MonthlyRevenue> monthlyBreakdown;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private BigDecimal revenue;
        private int bookingCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookingsReport {
        private int totalBookings;
        private int confirmedBookings;
        private int cancelledBookings;
        private int completedBookings;
        private Map<String, Integer> bookingsByGround;
        private List<DailyBookings> dailyBreakdown;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyBookings {
        private String date;
        private int bookingCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomersReport {
        private int totalCustomers;
        private int newCustomers;
        private int returningCustomers;
        private List<TopCustomer> topCustomers;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopCustomer {
        private String customerName;
        private String email;
        private int bookingCount;
        private BigDecimal totalSpent;
    }
}
