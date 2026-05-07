package com.amrit.futsal.service;

import com.amrit.futsal.dto.*;
import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final FutsalCompanyRepository futsalCompanyRepository;
    private final FutsalGroundRepository futsalGroundRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public AdminAnalyticsService(
            UserRepository userRepository,
            FutsalCompanyRepository futsalCompanyRepository,
            FutsalGroundRepository futsalGroundRepository,
            TimeSlotRepository timeSlotRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.futsalCompanyRepository = futsalCompanyRepository;
        this.futsalGroundRepository = futsalGroundRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
    }

    public AdminStatsResponse getAdminStatistics() {
        AdminStatsResponse stats = new AdminStatsResponse();

        // User statistics
        stats.setTotalUsers(userRepository.count());
        stats.setTotalOwners(userRepository.countByRole(User.Role.OWNER));
        stats.setTotalCustomers(userRepository.countByRole(User.Role.USER));
        stats.setTotalAdmins(userRepository.countByRole(User.Role.ADMIN));

        // Company and ground statistics
        stats.setTotalCompanies(futsalCompanyRepository.count());
        stats.setTotalGrounds(futsalGroundRepository.count());
        stats.setTotalTimeSlots(timeSlotRepository.count());

        // Booking statistics
        stats.setTotalBookings(bookingRepository.count());
        stats.setConfirmedBookings(bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED));
        stats.setCancelledBookings(bookingRepository.countByStatus(Booking.BookingStatus.CANCELLED));
        stats.setCompletedBookings(bookingRepository.countByStatus(Booking.BookingStatus.COMPLETED));

        // Revenue statistics
        BigDecimal totalRevenue = paymentRepository.sumTotalAmount();
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        BigDecimal pendingRevenue = paymentRepository.sumAmountByStatus(Payment.PaymentStatus.PENDING);
        stats.setPendingRevenue(pendingRevenue != null ? pendingRevenue : BigDecimal.ZERO);

        BigDecimal successRevenue = paymentRepository.sumAmountByStatus(Payment.PaymentStatus.SUCCESS);
        stats.setSuccessRevenue(successRevenue != null ? successRevenue : BigDecimal.ZERO);

        // Review statistics
        stats.setTotalReviews(reviewRepository.count());
        Double averageRating = reviewRepository.findAll().stream()
                .mapToInt(r -> r.getRating())
                .average()
                .orElse(0.0);
        stats.setAverageRating(Math.round(averageRating * 100.0) / 100.0);

        // Recent activity (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long recentBookings = bookingRepository.findByBookingDateBetween(sevenDaysAgo, LocalDateTime.now()).size();
        stats.setRecentActivityCount(recentBookings);

        return stats;
    }

    public RevenueAnalyticsResponse getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        final LocalDateTime effectiveStart = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        final LocalDateTime effectiveEnd = endDate != null ? endDate : LocalDateTime.now();

        RevenueAnalyticsResponse analytics = new RevenueAnalyticsResponse();

        // Total revenue in date range
        List<Payment> paymentsInRange = paymentRepository.findAll().stream()
                .filter(p -> {
                    LocalDateTime bookingDate = p.getBooking().getBookingDate();
                    return !bookingDate.isBefore(effectiveStart) && !bookingDate.isAfter(effectiveEnd);
                })
                .collect(Collectors.toList());

        BigDecimal totalRevenue = paymentsInRange.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.setTotalRevenue(totalRevenue);

        // Monthly revenue (last 30 days)
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        BigDecimal monthlyRevenue = paymentRepository.findAll().stream()
                .filter(p -> {
                    LocalDateTime bookingDate = p.getBooking().getBookingDate();
                    return !bookingDate.isBefore(monthAgo) && p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS;
                })
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.setMonthlyRevenue(monthlyRevenue);

        // Daily revenue (last 24 hours)
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        BigDecimal dailyRevenue = paymentRepository.findAll().stream()
                .filter(p -> {
                    LocalDateTime bookingDate = p.getBooking().getBookingDate();
                    return !bookingDate.isBefore(dayAgo) && p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS;
                })
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.setDailyRevenue(dailyRevenue);

        // Revenue by ground
        Map<String, BigDecimal> revenueByGround = paymentsInRange.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        p -> p.getBooking().getGround().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));
        analytics.setRevenueByGround(revenueByGround);

        // Revenue by company
        Map<String, BigDecimal> revenueByCompany = paymentsInRange.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        p -> p.getBooking().getGround().getCompany().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));
        analytics.setRevenueByCompany(revenueByCompany);

        // Revenue by status
        Map<Payment.PaymentStatus, BigDecimal> revenueByStatus = paymentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Payment::getPaymentStatus,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));
        analytics.setRevenueByStatus(revenueByStatus);

        // Revenue growth (compare current period to previous period)
        LocalDateTime previousPeriodStart = effectiveStart.minusDays(effectiveEnd.toLocalDate().toEpochDay() - effectiveStart.toLocalDate().toEpochDay());
        BigDecimal previousRevenue = paymentRepository.findAll().stream()
                .filter(p -> {
                    LocalDateTime bookingDate = p.getBooking().getBookingDate();
                    return !bookingDate.isBefore(previousPeriodStart) && !bookingDate.isAfter(effectiveStart) &&
                            p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS;
                })
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double growth = 0.0;
        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = totalRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        analytics.setRevenueGrowth(Math.round(growth * 100.0) / 100.0);

        return analytics;
    }

    public BookingAnalyticsResponse getBookingAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        BookingAnalyticsResponse analytics = new BookingAnalyticsResponse();

        List<Booking> bookingsInRange = bookingRepository.findByBookingDateBetween(startDate, endDate);
        analytics.setTotalBookings((long) bookingsInRange.size());

        // Bookings by status
        Map<Booking.BookingStatus, Long> bookingsByStatus = bookingsInRange.stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
        analytics.setBookingsByStatus(bookingsByStatus);

        // Bookings by ground
        Map<String, Long> bookingsByGround = bookingsInRange.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getGround().getName(),
                        Collectors.counting()
                ));
        analytics.setBookingsByGround(bookingsByGround);

        // Bookings by day of week
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        Map<String, Long> bookingsByDay = bookingsInRange.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getSlot().getStartTime().format(dayFormatter),
                        Collectors.counting()
                ));
        analytics.setBookingsByDay(bookingsByDay);

        // Peak hours (top 5 hours with most bookings)
        Map<Integer, Long> bookingsByHour = bookingsInRange.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getSlot().getStartTime().getHour(),
                        Collectors.counting()
                ));

        List<BookingAnalyticsResponse.PeakHour> peakHours = bookingsByHour.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new BookingAnalyticsResponse.PeakHour(e.getKey() + ":00", e.getValue()))
                .collect(Collectors.toList());
        analytics.setPeakHours(peakHours);

        // Average booking value
        BigDecimal totalValue = bookingsInRange.stream()
                .map(b -> paymentRepository.findByBookingId(b.getId()).stream()
                        .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                        .findFirst()
                        .map(Payment::getAmount)
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgValue = bookingsInRange.isEmpty() ? BigDecimal.ZERO :
                totalValue.divide(BigDecimal.valueOf(bookingsInRange.size()), 2, RoundingMode.HALF_UP);
        analytics.setAverageBookingValue(avgValue);

        // Booking trends (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Booking> recentBookings = bookingRepository.findByBookingDateBetween(thirtyDaysAgo, LocalDateTime.now());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> bookingsByDate = recentBookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().format(dateFormatter),
                        Collectors.counting()
                ));

        List<BookingAnalyticsResponse.BookingTrend> trends = bookingsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new BookingAnalyticsResponse.BookingTrend(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        analytics.setBookingTrends(trends);

        return analytics;
    }

    public UserAnalyticsResponse getUserAnalytics() {
        UserAnalyticsResponse analytics = new UserAnalyticsResponse();

        analytics.setTotalUsers(userRepository.count());

        // New users this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(startOfMonth))
                .count();
        analytics.setNewUsersThisMonth(newUsers);

        // Active users (users with bookings in last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Booking> recentBookings = bookingRepository.findByBookingDateBetween(thirtyDaysAgo, LocalDateTime.now());
        long activeUsers = recentBookings.stream()
                .map(b -> b.getUser().getId())
                .distinct()
                .count();
        analytics.setActiveUsers(activeUsers);

        // Users by role
        Map<User.Role, Long> usersByRole = new HashMap<>();
        for (User.Role role : User.Role.values()) {
            usersByRole.put(role, userRepository.countByRole(role));
        }
        analytics.setUsersByRole(usersByRole);

        // User growth rate (compare current month to previous month)
        LocalDateTime startOfPreviousMonth = startOfMonth.minusMonths(1);
        long previousMonthUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isBefore(startOfMonth))
                .count();

        Double growthRate = 0.0;
        if (previousMonthUsers > 0) {
            growthRate = ((double) newUsers / previousMonthUsers) * 100;
        }
        analytics.setUserGrowthRate(Math.round(growthRate * 100.0) / 100.0);

        // Top customers (by total bookings and spending)
        List<UserAnalyticsResponse.TopCustomer> topCustomers = userRepository.findByRole(User.Role.USER).stream()
                .map(user -> {
                    List<Booking> userBookings = bookingRepository.findByUserId(user.getId());
                    long totalBookings = userBookings.size();

                    BigDecimal totalSpent = userBookings.stream()
                            .flatMap(b -> paymentRepository.findByBookingId(b.getId()).stream())
                            .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new UserAnalyticsResponse.TopCustomer(
                            user.getId().toString(),
                            user.getName(),
                            user.getEmail(),
                            totalBookings,
                            totalSpent
                    );
                })
                .filter(tc -> tc.getTotalBookings() > 0)
                .sorted(Comparator.comparing(UserAnalyticsResponse.TopCustomer::getTotalSpent).reversed())
                .limit(10)
                .collect(Collectors.toList());

        analytics.setTopCustomers(topCustomers);

        return analytics;
    }
}
