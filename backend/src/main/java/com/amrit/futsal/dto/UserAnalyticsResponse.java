package com.amrit.futsal.dto;

import com.amrit.futsal.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsResponse {

    private Long totalUsers;
    private Long newUsersThisMonth;
    private Long activeUsers;
    private Map<User.Role, Long> usersByRole;
    private Double userGrowthRate; // Percentage growth
    private List<TopCustomer> topCustomers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopCustomer {
        private String id;
        private String name;
        private String email;
        private Long totalBookings;
        private BigDecimal totalSpent;
    }
}
