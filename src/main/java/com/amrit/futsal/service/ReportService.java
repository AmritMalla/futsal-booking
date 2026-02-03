package com.amrit.futsal.service;

import com.amrit.futsal.dto.ReportData;
import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.Report;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.PaymentRepository;
import com.amrit.futsal.repository.ReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ReportService(ReportRepository reportRepository,
                         BookingRepository bookingRepository,
                         PaymentRepository paymentRepository) {
        this.reportRepository = reportRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
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

    public Report generateRevenueReport(User owner) {
        return generateRevenueReport(owner, LocalDateTime.now().minusMonths(1), LocalDateTime.now());
    }

    public Report generateRevenueReport(User owner, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findByOwnerIdAndDateRange(
                owner.getId(), startDate, endDate);

        // Calculate revenue data
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, BigDecimal> revenueByGround = new HashMap<>();
        Map<String, List<Booking>> bookingsByMonth = new HashMap<>();

        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.COMPLETED ||
                    booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                BigDecimal price = booking.getGround().getPricePerHour();
                totalRevenue = totalRevenue.add(price);

                String groundName = booking.getGround().getName();
                revenueByGround.merge(groundName, price, BigDecimal::add);

                String month = booking.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                bookingsByMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(booking);
            }
        }

        int totalBookings = bookings.size();
        BigDecimal averageBookingValue = totalBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<ReportData.MonthlyRevenue> monthlyBreakdown = bookingsByMonth.entrySet().stream()
                .map(entry -> {
                    BigDecimal monthRevenue = entry.getValue().stream()
                            .map(b -> b.getGround().getPricePerHour())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ReportData.MonthlyRevenue(entry.getKey(), monthRevenue, entry.getValue().size());
                })
                .sorted(Comparator.comparing(ReportData.MonthlyRevenue::getMonth))
                .collect(Collectors.toList());

        ReportData.RevenueReport revenueReport = new ReportData.RevenueReport(
                totalRevenue, totalBookings, averageBookingValue, revenueByGround, monthlyBreakdown);

        Report report = new Report();
        report.setOwner(owner);
        report.setReportType(Report.ReportType.REVENUE);
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        try {
            report.setReportData(objectMapper.writeValueAsString(revenueReport));
        } catch (JsonProcessingException e) {
            report.setReportData("{}");
        }

        return reportRepository.save(report);
    }

    public Report generateBookingsReport(User owner) {
        return generateBookingsReport(owner, LocalDateTime.now().minusMonths(1), LocalDateTime.now());
    }

    public Report generateBookingsReport(User owner, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findByOwnerIdAndDateRange(
                owner.getId(), startDate, endDate);

        int totalBookings = bookings.size();
        int confirmedBookings = (int) bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED).count();
        int cancelledBookings = (int) bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLED).count();
        int completedBookings = (int) bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED).count();

        Map<String, Integer> bookingsByGround = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getGround().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        Map<String, List<Booking>> bookingsByDate = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

        List<ReportData.DailyBookings> dailyBreakdown = bookingsByDate.entrySet().stream()
                .map(entry -> new ReportData.DailyBookings(entry.getKey(), entry.getValue().size()))
                .sorted(Comparator.comparing(ReportData.DailyBookings::getDate))
                .collect(Collectors.toList());

        ReportData.BookingsReport bookingsReport = new ReportData.BookingsReport(
                totalBookings, confirmedBookings, cancelledBookings, completedBookings,
                bookingsByGround, dailyBreakdown);

        Report report = new Report();
        report.setOwner(owner);
        report.setReportType(Report.ReportType.BOOKINGS);
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        try {
            report.setReportData(objectMapper.writeValueAsString(bookingsReport));
        } catch (JsonProcessingException e) {
            report.setReportData("{}");
        }

        return reportRepository.save(report);
    }

    public Report generateCustomersReport(User owner) {
        return generateCustomersReport(owner, LocalDateTime.now().minusMonths(1), LocalDateTime.now());
    }

    public Report generateCustomersReport(User owner, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> allBookings = bookingRepository.findByOwnerId(owner.getId());
        List<Booking> periodBookings = bookingRepository.findByOwnerIdAndDateRange(
                owner.getId(), startDate, endDate);

        // Get unique customers
        Set<UUID> allCustomerIds = allBookings.stream()
                .map(b -> b.getUser().getId())
                .collect(Collectors.toSet());

        Set<UUID> periodCustomerIds = periodBookings.stream()
                .map(b -> b.getUser().getId())
                .collect(Collectors.toSet());

        // Find customers who had bookings before the period
        Set<UUID> previousCustomerIds = allBookings.stream()
                .filter(b -> b.getBookingDate().isBefore(startDate))
                .map(b -> b.getUser().getId())
                .collect(Collectors.toSet());

        int newCustomers = (int) periodCustomerIds.stream()
                .filter(id -> !previousCustomerIds.contains(id))
                .count();

        int returningCustomers = periodCustomerIds.size() - newCustomers;

        // Calculate top customers
        Map<User, List<Booking>> bookingsByUser = periodBookings.stream()
                .collect(Collectors.groupingBy(Booking::getUser));

        List<ReportData.TopCustomer> topCustomers = bookingsByUser.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<Booking> userBookings = entry.getValue();
                    BigDecimal totalSpent = userBookings.stream()
                            .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                            .map(b -> b.getGround().getPricePerHour())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ReportData.TopCustomer(
                            user.getName(), user.getEmail(), userBookings.size(), totalSpent);
                })
                .sorted((a, b) -> b.getBookingCount() - a.getBookingCount())
                .limit(10)
                .collect(Collectors.toList());

        ReportData.CustomersReport customersReport = new ReportData.CustomersReport(
                periodCustomerIds.size(), newCustomers, returningCustomers, topCustomers);

        Report report = new Report();
        report.setOwner(owner);
        report.setReportType(Report.ReportType.CUSTOMERS);
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        try {
            report.setReportData(objectMapper.writeValueAsString(customersReport));
        } catch (JsonProcessingException e) {
            report.setReportData("{}");
        }

        return reportRepository.save(report);
    }

    public void deleteReport(UUID reportId) {
        reportRepository.deleteById(reportId);
    }
}
