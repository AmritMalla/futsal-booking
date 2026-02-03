package com.amrit.futsal.api;

import com.amrit.futsal.dto.ReportResponse;
import com.amrit.futsal.entity.Report;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.service.ReportService;
import com.amrit.futsal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    @Autowired
    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable UUID reportId) {
        Report report = reportService.getReportById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
        return ResponseEntity.ok(ReportResponse.fromEntity(report));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ReportResponse>> getReportsByOwnerId(@PathVariable UUID ownerId) {
        List<ReportResponse> reports = reportService.getReportsByOwnerId(ownerId)
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/type/{reportType}")
    public ResponseEntity<List<ReportResponse>> getReportsByType(@PathVariable Report.ReportType reportType) {
        List<ReportResponse> reports = reportService.getReportsByType(reportType)
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ReportResponse>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<ReportResponse> reports = reportService.getReportsByDateRange(start, end)
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/generate/revenue/{ownerId}")
    public ResponseEntity<ReportResponse> generateRevenueReport(
            @PathVariable UUID ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        User owner = validateOwner(ownerId);

        Report report;
        if (startDate != null && endDate != null) {
            report = reportService.generateRevenueReport(owner, startDate, endDate);
        } else {
            report = reportService.generateRevenueReport(owner);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ReportResponse.fromEntity(report));
    }

    @PostMapping("/generate/bookings/{ownerId}")
    public ResponseEntity<ReportResponse> generateBookingsReport(
            @PathVariable UUID ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        User owner = validateOwner(ownerId);

        Report report;
        if (startDate != null && endDate != null) {
            report = reportService.generateBookingsReport(owner, startDate, endDate);
        } else {
            report = reportService.generateBookingsReport(owner);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ReportResponse.fromEntity(report));
    }

    @PostMapping("/generate/customers/{ownerId}")
    public ResponseEntity<ReportResponse> generateCustomersReport(
            @PathVariable UUID ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        User owner = validateOwner(ownerId);

        Report report;
        if (startDate != null && endDate != null) {
            report = reportService.generateCustomersReport(owner, startDate, endDate);
        } else {
            report = reportService.generateCustomersReport(owner);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ReportResponse.fromEntity(report));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        if (reportService.getReportById(reportId).isEmpty()) {
            throw new ResourceNotFoundException("Report", "id", reportId);
        }
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    private User validateOwner(UUID ownerId) {
        User owner = userService.getUserById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        if (owner.getRole() != User.Role.OWNER) {
            throw new BadRequestException("User is not an owner. Reports can only be generated for owners.");
        }

        return owner;
    }
}
