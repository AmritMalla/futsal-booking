package com.amrit.futsal.api;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.Review;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.PaymentRepository;
import com.amrit.futsal.repository.ReviewRepository;
import com.amrit.futsal.repository.TimeSlotRepository;
import com.amrit.futsal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FutsalCompanyRepository companyRepository;

    @Autowired
    private FutsalGroundRepository groundRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @WithMockUser(username = "owner.analytics@example.com", roles = "OWNER")
    void ownerCannotAccessAdminAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin.analytics@example.com", roles = "ADMIN")
    void adminStatsReflectSeededDashboardData() throws Exception {
        saveUser("Admin User", "admin.analytics@example.com", User.Role.ADMIN);

        User owner = saveUser("Owner User", uniqueEmail("owner"), User.Role.OWNER);
        User customer = saveUser("Customer User", uniqueEmail("customer"), User.Role.USER);

        FutsalCompany company = saveCompany(owner, "Arena Company");
        FutsalGround ground = saveGround(company, "Arena Ground", BigDecimal.valueOf(1500));
        TimeSlot confirmedSlot = saveSlot(ground, LocalDateTime.now().plusDays(1), true);
        TimeSlot cancelledSlot = saveSlot(ground, LocalDateTime.now().plusDays(2), false);
        TimeSlot completedSlot = saveSlot(ground, LocalDateTime.now().plusDays(3), true);

        Booking confirmedBooking = saveBooking(customer, ground, confirmedSlot, Booking.BookingStatus.CONFIRMED, LocalDateTime.now().minusDays(1));
        Booking cancelledBooking = saveBooking(customer, ground, cancelledSlot, Booking.BookingStatus.CANCELLED, LocalDateTime.now().minusDays(2));
        Booking completedBooking = saveBooking(customer, ground, completedSlot, Booking.BookingStatus.COMPLETED, LocalDateTime.now().minusDays(3));

        savePayment(customer, confirmedBooking, BigDecimal.valueOf(1500), Payment.PaymentStatus.SUCCESS);
        savePayment(customer, cancelledBooking, BigDecimal.valueOf(500), Payment.PaymentStatus.PENDING);
        savePayment(customer, completedBooking, BigDecimal.valueOf(1200), Payment.PaymentStatus.SUCCESS);

        saveReview(customer, ground, 4, "Nice ground");
        saveReview(owner, ground, 5, "Owner-inspected");

        long totalUsers = userRepository.count();
        long totalOwners = userRepository.countByRole(User.Role.OWNER);
        long totalCustomers = userRepository.countByRole(User.Role.USER);
        long totalAdmins = userRepository.countByRole(User.Role.ADMIN);
        long totalCompanies = companyRepository.count();
        long totalGrounds = groundRepository.count();
        long totalSlots = timeSlotRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED);
        long cancelledBookings = bookingRepository.countByStatus(Booking.BookingStatus.CANCELLED);
        long completedBookings = bookingRepository.countByStatus(Booking.BookingStatus.COMPLETED);
        long totalReviews = reviewRepository.count();

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(totalUsers))
                .andExpect(jsonPath("$.totalOwners").value(totalOwners))
                .andExpect(jsonPath("$.totalCustomers").value(totalCustomers))
                .andExpect(jsonPath("$.totalAdmins").value(totalAdmins))
                .andExpect(jsonPath("$.totalCompanies").value(totalCompanies))
                .andExpect(jsonPath("$.totalGrounds").value(totalGrounds))
                .andExpect(jsonPath("$.totalTimeSlots").value(totalSlots))
                .andExpect(jsonPath("$.totalBookings").value(totalBookings))
                .andExpect(jsonPath("$.confirmedBookings").value(confirmedBookings))
                .andExpect(jsonPath("$.cancelledBookings").value(cancelledBookings))
                .andExpect(jsonPath("$.completedBookings").value(completedBookings))
                .andExpect(jsonPath("$.totalRevenue").value(3200))
                .andExpect(jsonPath("$.pendingRevenue").value(500))
                .andExpect(jsonPath("$.successRevenue").value(2700))
                .andExpect(jsonPath("$.totalReviews").value(totalReviews))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.recentActivityCount").value(3));
    }

    @Test
    @WithMockUser(username = "admin.analytics@example.com", roles = "ADMIN")
    void adminAnalyticsEndpointsReturnRevenueBookingAndUserBreakdowns() throws Exception {
        saveUser("Admin User", "admin.analytics@example.com", User.Role.ADMIN);

        User owner = saveUser("Owner User", uniqueEmail("owner"), User.Role.OWNER);
        User customerOne = saveUser("Customer One", uniqueEmail("customer"), User.Role.USER);
        User customerTwo = saveUser("Customer Two", uniqueEmail("customer"), User.Role.USER);

        FutsalCompany company = saveCompany(owner, "Prime Arena");
        FutsalGround ground = saveGround(company, "Prime Ground", BigDecimal.valueOf(1800));

        TimeSlot slotOne = saveSlot(ground, LocalDateTime.now().plusDays(1).withHour(18), true);
        TimeSlot slotTwo = saveSlot(ground, LocalDateTime.now().plusDays(2).withHour(20), true);

        LocalDateTime rangeStart = LocalDateTime.now().minusDays(5);
        LocalDateTime rangeEnd = LocalDateTime.now().plusDays(1);

        Booking bookingOne = saveBooking(customerOne, ground, slotOne, Booking.BookingStatus.CONFIRMED, LocalDateTime.now().minusDays(1));
        Booking bookingTwo = saveBooking(customerTwo, ground, slotTwo, Booking.BookingStatus.COMPLETED, LocalDateTime.now().minusDays(2));

        savePayment(customerOne, bookingOne, BigDecimal.valueOf(1800), Payment.PaymentStatus.SUCCESS);
        savePayment(customerTwo, bookingTwo, BigDecimal.valueOf(2000), Payment.PaymentStatus.FAILED);

        long totalUsers = userRepository.count();
        long newUsersThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)))
                .count();

        mockMvc.perform(get("/api/v1/admin/analytics/revenue")
                        .param("startDate", rangeStart.toString())
                        .param("endDate", rangeEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(1800))
                .andExpect(jsonPath("$.revenueByGround['Prime Ground']").value(1800))
                .andExpect(jsonPath("$.revenueByCompany['Prime Arena']").value(1800))
                .andExpect(jsonPath("$.revenueByStatus.SUCCESS").value(1800))
                .andExpect(jsonPath("$.revenueByStatus.FAILED").value(2000));

        mockMvc.perform(get("/api/v1/admin/analytics/bookings")
                        .param("startDate", rangeStart.toString())
                        .param("endDate", rangeEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(2))
                .andExpect(jsonPath("$.bookingsByStatus.CONFIRMED").value(1))
                .andExpect(jsonPath("$.bookingsByStatus.COMPLETED").value(1))
                .andExpect(jsonPath("$.bookingsByGround['Prime Ground']").value(2))
                .andExpect(jsonPath("$.peakHours[0].count").value(1))
                .andExpect(jsonPath("$.averageBookingValue").value(900.00))
                .andExpect(jsonPath("$.bookingTrends.length()").value(2));

        mockMvc.perform(get("/api/v1/admin/analytics/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(totalUsers))
                .andExpect(jsonPath("$.newUsersThisMonth").value(newUsersThisMonth))
                .andExpect(jsonPath("$.activeUsers").value(2))
                .andExpect(jsonPath("$.usersByRole.ADMIN").value(userRepository.countByRole(User.Role.ADMIN)))
                .andExpect(jsonPath("$.usersByRole.OWNER").value(userRepository.countByRole(User.Role.OWNER)))
                .andExpect(jsonPath("$.usersByRole.USER").value(userRepository.countByRole(User.Role.USER)))
                .andExpect(jsonPath("$.topCustomers[*].email", hasItems(customerOne.getEmail(), customerTwo.getEmail())));
    }

    private User saveUser(String name, String email, User.Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setPhoneNumber(uniquePhone());
        user.setRole(role);
        return userRepository.save(user);
    }

    private FutsalCompany saveCompany(User owner, String name) {
        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName(name);
        company.setLocation("Kathmandu");
        return companyRepository.save(company);
    }

    private FutsalGround saveGround(FutsalCompany company, String name, BigDecimal pricePerHour) {
        FutsalGround ground = new FutsalGround();
        ground.setCompany(company);
        ground.setName(name);
        ground.setSurfaceType("Indoor");
        ground.setPricePerHour(pricePerHour);
        return groundRepository.save(ground);
    }

    private TimeSlot saveSlot(FutsalGround ground, LocalDateTime startTime, boolean isBooked) {
        TimeSlot slot = new TimeSlot();
        slot.setGround(ground);
        slot.setStartTime(startTime);
        slot.setEndTime(startTime.plusHours(1));
        slot.setIsBooked(isBooked);
        return timeSlotRepository.save(slot);
    }

    private Booking saveBooking(User user,
                                FutsalGround ground,
                                TimeSlot slot,
                                Booking.BookingStatus status,
                                LocalDateTime bookingDate) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setGround(ground);
        booking.setSlot(slot);
        booking.setBookingDate(bookingDate);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private Payment savePayment(User user,
                                Booking booking,
                                BigDecimal amount,
                                Payment.PaymentStatus status) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentStatus(status);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return paymentRepository.save(payment);
    }

    private Review saveReview(User user, FutsalGround ground, int rating, String text) {
        Review review = new Review();
        review.setUser(user);
        review.setGround(ground);
        review.setRating(rating);
        review.setReviewText(text);
        return reviewRepository.save(review);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.com";
    }

    private String uniquePhone() {
        String digits = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        return String.format("%1$-10.10s", digits).replace(' ', '0');
    }
}
