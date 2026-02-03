package com.amrit.futsal.service;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.Report;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final BookingService bookingService;

    @Autowired
    public ReportService(ReportRepository reportRepository, BookingService bookingService) {
        this.reportRepository = reportRepository;
        this.bookingService = bookingService;
    }

    public Report createReport(Report report) {
        return reportRepository.save(report);
    }

    public Optional<Report> getReportById(UUID reportId) {
        return reportRepository.findById(reportId);
    }

    public List<Report> getReportsByOwnerId(UUID ownerId) {
        return reportRepository.findByOwnerId(ownerId);
    }
    
    public List<Report> getReportsByType(Report.ReportType reportType) {
        return reportRepository.findByReportType(reportType);
    }
    
    public List<Report> getReportsByDateRange(LocalDateTime start, LocalDateTime end) {
        return reportRepository.findByGeneratedAtBetween(start, end);
    }
    
    public List<Report> getReportsByOwnerAndType(UUID ownerId, Report.ReportType reportType) {
        return reportRepository.findByOwnerIdAndReportType(ownerId, reportType);
    }
    
    /**
     * Generates a revenue report for a specific owner
     */
    public Report generateRevenueReport(User owner) {
        // Implementation will depend on how you want to generate the report
        Report report = new Report();
        report.setOwner(owner);
        report.setReportType(Report.ReportType.REVENUE);
        
        // Save the report
        return reportRepository.save(report);
    }
    
    /**
     * Generates a bookings report for a specific owner
     */
    public Report generateBookingsReport(User owner) {
        // Implementation will depend on how you want to generate the report
        Report report = new Report();
        report.setOwner(owner);
        report.setReportType(Report.ReportType.BOOKINGS);
        
        // Save the report
        return reportRepository.save(report);
    }
    
    /**
     * Generates a customers report for a specific owner
     */
    public Report generateCustomersReport(User owner) {
        // Implementation will depend on how you want to generate the report
        Report report = new Report();
        report.setOwner(owner);
        report.setReportType(Report.ReportType.CUSTOMERS);
        
        // Save the report
        return reportRepository.save(report);
    }
    
    public void deleteReport(UUID reportId) {
        reportRepository.deleteById(reportId);
    }
}
