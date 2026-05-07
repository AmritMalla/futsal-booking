package com.amrit.futsal.dto;

import com.amrit.futsal.entity.Report;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {

    private UUID id;
    private UUID ownerId;
    private String ownerName;
    private Report.ReportType reportType;
    private Object reportData;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime generatedAt;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static ReportResponse fromEntity(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setOwnerId(report.getOwner().getId());
        response.setOwnerName(report.getOwner().getName());
        response.setReportType(report.getReportType());
        response.setStartDate(report.getStartDate());
        response.setEndDate(report.getEndDate());
        response.setGeneratedAt(report.getGeneratedAt());

        if (report.getReportData() != null) {
            try {
                switch (report.getReportType()) {
                    case REVENUE:
                        response.setReportData(objectMapper.readValue(
                                report.getReportData(), ReportData.RevenueReport.class));
                        break;
                    case BOOKINGS:
                        response.setReportData(objectMapper.readValue(
                                report.getReportData(), ReportData.BookingsReport.class));
                        break;
                    case CUSTOMERS:
                        response.setReportData(objectMapper.readValue(
                                report.getReportData(), ReportData.CustomersReport.class));
                        break;
                }
            } catch (JsonProcessingException e) {
                response.setReportData(report.getReportData());
            }
        }

        return response;
    }
}
