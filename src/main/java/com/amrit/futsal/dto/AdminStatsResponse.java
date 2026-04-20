package com.amrit.futsal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatsResponse {

    private Long totalUsers;
    private Long totalOwners;
    private Long totalCustomers;
    private Long totalAdmins;
    private Long totalCompanies;
    private Long totalGrounds;
    private Long totalTimeSlots;
    private Long totalBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private Long completedBookings;
    private BigDecimal totalRevenue;
    private BigDecimal pendingRevenue;
    private BigDecimal successRevenue;
    private Long totalReviews;
    private Double averageRating;
    private Long recentActivityCount;
}
