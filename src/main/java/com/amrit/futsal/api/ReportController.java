package com.amrit.futsal.api;

import com.amrit.futsal.entity.Report;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.service.ReportService;
import com.amrit.futsal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public ResponseEntity<Report> getReportById(@PathVariable UUID reportId) {
        return reportService.getReportById(reportId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Report>> getReportsByOwnerId(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(reportService.getReportsByOwnerId(ownerId));
    }
    
    @GetMapping("/type/{reportType}")
    public ResponseEntity<List<Report>> getReportsByType(@PathVariable Report.ReportType reportType) {
        return ResponseEntity.ok(reportService.getReportsByType(reportType));
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<Report>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getReportsByDateRange(start, end));
    }
    
    @PostMapping("/generate/revenue/{ownerId}")
    public ResponseEntity<Report> generateRevenueReport(@PathVariable UUID ownerId) {
        Optional<User> ownerOpt = userService.getUserById(ownerId);
        if (ownerOpt.isPresent() && ownerOpt.get().getRole() == User.Role.OWNER) {
            Report report = reportService.generateRevenueReport(ownerOpt.get());
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/generate/bookings/{ownerId}")
    public ResponseEntity<Report> generateBookingsReport(@PathVariable UUID ownerId) {
        Optional<User> ownerOpt = userService.getUserById(ownerId);
        if (ownerOpt.isPresent() && ownerOpt.get().getRole() == User.Role.OWNER) {
            Report report = reportService.generateBookingsReport(ownerOpt.get());
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/generate/customers/{ownerId}")
    public ResponseEntity<Report> generateCustomersReport(@PathVariable UUID ownerId) {
        Optional<User> ownerOpt = userService.getUserById(ownerId);
        if (ownerOpt.isPresent() && ownerOpt.get().getRole() == User.Role.OWNER) {
            Report report = reportService.generateCustomersReport(ownerOpt.get());
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }
}
