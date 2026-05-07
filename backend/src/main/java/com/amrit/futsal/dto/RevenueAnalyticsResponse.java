package com.amrit.futsal.dto;

import com.amrit.futsal.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueAnalyticsResponse {

    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal dailyRevenue;
    private Map<String, BigDecimal> revenueByGround;
    private Map<String, BigDecimal> revenueByCompany;
    private Map<Payment.PaymentStatus, BigDecimal> revenueByStatus;
    private Double revenueGrowth; // Percentage change from previous period
}
