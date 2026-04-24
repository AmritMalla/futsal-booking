package com.amrit.futsal.api;

import com.amrit.futsal.dto.ReportResponse;
import com.amrit.futsal.entity.Report;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.service.AuthenticatedUserService;
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
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public ReportController(ReportService reportService,
                            UserService userService,
                            AuthenticatedUserService authenticatedUserService) {
        this.reportService = reportService;
        this.userService = userService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable UUID reportId) {
        authenticatedUserService.requireReportOwnerOrAdmin(reportId);
        Report report = reportService.getReportById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
        return ResponseEntity.ok(ReportResponse.fromEntity(report));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ReportResponse>> getReportsByOwnerId(@PathVariable UUID ownerId) {
        authenticatedUserService.requireCurrentUserOrAdmin(ownerId);
        List<ReportResponse> reports = reportService.getReportsByOwnerId(ownerId)
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        User currentUser = requireOwnerUser();
        List<ReportResponse> reports = reportService.getReportsByOwnerId(currentUser.getId())
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/type/{reportType}")
    public ResponseEntity<List<ReportResponse>> getReportsByType(@PathVariable Report.ReportType reportType) {
        authenticatedUserService.requireAdmin();
        List<ReportResponse> reports = reportService.getReportsByType(reportType)
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/me/type/{reportType}")
    public ResponseEntity<List<ReportResponse>> getMyReportsByType(@PathVariable Report.ReportType reportType) {
        User currentUser = requireOwnerUser();
        List<ReportResponse> reports = reportService.getReportsByOwnerAndType(currentUser.getId(), reportType)
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ReportResponse>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        authenticatedUserService.requireAdmin();
        List<ReportResponse> reports = reportService.getReportsByDateRange(start, end)
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/me/date-range")
    public ResponseEntity<List<ReportResponse>> getMyReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        User currentUser = requireOwnerUser();
        List<ReportResponse> reports = reportService.getReportsByOwnerAndDateRange(currentUser.getId(), start, end)
                .stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/generate/revenue")
    public ResponseEntity<ReportResponse> generateMyRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReportResponse.fromEntity(generateRevenueReportForOwner(requireOwnerUser(), startDate, endDate)));
    }

    @PostMapping("/generate/revenue/{ownerId}")
    public ResponseEntity<ReportResponse> generateRevenueReport(
            @PathVariable UUID ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        authenticatedUserService.requireCurrentUserOrAdmin(ownerId);
        User owner = validateOwner(ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReportResponse.fromEntity(generateRevenueReportForOwner(owner, startDate, endDate)));
    }

    @PostMapping("/generate/bookings")
    public ResponseEntity<ReportResponse> generateMyBookingsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReportResponse.fromEntity(generateBookingsReportForOwner(requireOwnerUser(), startDate, endDate)));
    }

    @PostMapping("/generate/bookings/{ownerId}")
    public ResponseEntity<ReportResponse> generateBookingsReport(
            @PathVariable UUID ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        authenticatedUserService.requireCurrentUserOrAdmin(ownerId);
        User owner = validateOwner(ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReportResponse.fromEntity(generateBookingsReportForOwner(owner, startDate, endDate)));
    }

    @PostMapping("/generate/customers")
    public ResponseEntity<ReportResponse> generateMyCustomersReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReportResponse.fromEntity(generateCustomersReportForOwner(requireOwnerUser(), startDate, endDate)));
    }

    @PostMapping("/generate/customers/{ownerId}")
    public ResponseEntity<ReportResponse> generateCustomersReport(
            @PathVariable UUID ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        authenticatedUserService.requireCurrentUserOrAdmin(ownerId);
        User owner = validateOwner(ownerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReportResponse.fromEntity(generateCustomersReportForOwner(owner, startDate, endDate)));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        authenticatedUserService.requireReportOwnerOrAdmin(reportId);
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

    private User requireOwnerUser() {
        User currentUser = authenticatedUserService.getCurrentUser();
        if (currentUser.getRole() != User.Role.OWNER) {
            throw new BadRequestException("Only owners can use self-service report endpoints.");
        }
        return currentUser;
    }

    private Report generateRevenueReportForOwner(User owner, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return reportService.generateRevenueReport(owner, startDate, endDate);
        }
        return reportService.generateRevenueReport(owner);
    }

    private Report generateBookingsReportForOwner(User owner, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return reportService.generateBookingsReport(owner, startDate, endDate);
        }
        return reportService.generateBookingsReport(owner);
    }

    private Report generateCustomersReportForOwner(User owner, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return reportService.generateCustomersReport(owner, startDate, endDate);
        }
        return reportService.generateCustomersReport(owner);
    }
}
