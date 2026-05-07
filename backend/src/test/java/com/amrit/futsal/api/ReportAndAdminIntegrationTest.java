package com.amrit.futsal.api;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.Report;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.PaymentRepository;
import com.amrit.futsal.repository.ReportRepository;
import com.amrit.futsal.repository.TimeSlotRepository;
import com.amrit.futsal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportAndAdminIntegrationTest {

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
    private ReportRepository reportRepository;

    @Test
    @WithMockUser(username = "booking.user@example.com", roles = "USER")
    void bookingUserCanCancelOwnBooking() throws Exception {
        User bookingUser = saveUser("Booking User", "booking.user@example.com", User.Role.USER);
        Booking booking = saveBooking(bookingUser, Booking.BookingStatus.CONFIRMED, true);

        mockMvc.perform(post("/api/v1/bookings/%s/cancel".formatted(booking.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        TimeSlot updatedSlot = timeSlotRepository.findById(booking.getSlot().getId()).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(Booking.BookingStatus.CANCELLED);
        assertThat(updatedSlot.getIsBooked()).isFalse();
    }

    @Test
    @WithMockUser(username = "admin.status@example.com", roles = "ADMIN")
    void adminCanUpdatePaymentStatusToFailed() throws Exception {
        saveUser("Admin User", "admin.status@example.com", User.Role.ADMIN);
        User bookingUser = saveUser("Payment User", uniqueEmail("payer"), User.Role.USER);
        Booking booking = saveBooking(bookingUser, Booking.BookingStatus.CONFIRMED, true);
        Payment payment = savePayment(booking, bookingUser, Payment.PaymentStatus.PENDING);

        mockMvc.perform(patch("/api/v1/admin/payments/%s/status".formatted(payment.getId()))
                        .param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.paymentStatus").value("FAILED"));

        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        TimeSlot updatedSlot = timeSlotRepository.findById(booking.getSlot().getId()).orElseThrow();

        assertThat(updatedPayment.getPaymentStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        assertThat(updatedBooking.getStatus()).isEqualTo(Booking.BookingStatus.CANCELLED);
        assertThat(updatedSlot.getIsBooked()).isFalse();
    }

    @Test
    @WithMockUser(username = "admin.status@example.com", roles = "ADMIN")
    void adminCanMarkBookingCompleted() throws Exception {
        saveUser("Admin User", "admin.status@example.com", User.Role.ADMIN);
        User bookingUser = saveUser("Booking User", uniqueEmail("booker"), User.Role.USER);
        Booking booking = saveBooking(bookingUser, Booking.BookingStatus.CONFIRMED, true);

        mockMvc.perform(patch("/api/v1/admin/bookings/%s/status".formatted(booking.getId()))
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(Booking.BookingStatus.COMPLETED);
    }

    @Test
    @WithMockUser(username = "report.owner@example.com", roles = "OWNER")
    void ownerCanGenerateAndReadOwnReports() throws Exception {
        User owner = saveUser("Report Owner", "report.owner@example.com", User.Role.OWNER);
        saveOwnedBookingData(owner);

        mockMvc.perform(post("/api/v1/reports/generate/revenue"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(owner.getId().toString()))
                .andExpect(jsonPath("$.reportType").value("REVENUE"));

        Report report = reportRepository.findByOwnerId(owner.getId()).stream().findFirst().orElseThrow();

        mockMvc.perform(get("/api/v1/reports/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerId").value(owner.getId().toString()));

        mockMvc.perform(get("/api/v1/reports/%s".formatted(report.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(report.getId().toString()))
                .andExpect(jsonPath("$.ownerId").value(owner.getId().toString()));
    }

    @Test
    @WithMockUser(username = "other.owner@example.com", roles = "OWNER")
    void ownerCannotReadAnotherOwnersReport() throws Exception {
        User reportOwner = saveUser("Report Owner", uniqueEmail("report-owner"), User.Role.OWNER);
        User otherOwner = saveUser("Other Owner", "other.owner@example.com", User.Role.OWNER);
        Report report = saveReport(reportOwner, Report.ReportType.REVENUE);

        mockMvc.perform(get("/api/v1/reports/%s".formatted(report.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/reports/owner/%s".formatted(reportOwner.getId())))
                .andExpect(status().isForbidden());

        assertThat(otherOwner.getRole()).isEqualTo(User.Role.OWNER);
    }

    @Test
    @WithMockUser(username = "plain.report.user@example.com", roles = "USER")
    void regularUserIsBlockedFromOwnerSelfServiceReportsAtSecurityLayer() throws Exception {
        saveUser("Plain User", "plain.report.user@example.com", User.Role.USER);

        mockMvc.perform(get("/api/v1/reports/me"))
                .andExpect(status().isForbidden());
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

    private Booking saveBooking(User bookingUser, Booking.BookingStatus status, boolean slotBooked) {
        User owner = saveUser("Owner " + UUID.randomUUID(), uniqueEmail("owner"), User.Role.OWNER);
        FutsalCompany company = saveCompany(owner);
        FutsalGround ground = saveGround(company);
        TimeSlot slot = saveSlot(ground, slotBooked);

        Booking booking = new Booking();
        booking.setUser(bookingUser);
        booking.setGround(ground);
        booking.setSlot(slot);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private void saveOwnedBookingData(User owner) {
        User customer = saveUser("Customer " + UUID.randomUUID(), uniqueEmail("customer"), User.Role.USER);
        FutsalCompany company = saveCompany(owner);
        FutsalGround ground = saveGround(company);
        TimeSlot slot = saveSlot(ground, true);

        Booking booking = new Booking();
        booking.setUser(customer);
        booking.setGround(ground);
        booking.setSlot(slot);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    private Payment savePayment(Booking booking, User user, Payment.PaymentStatus status) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(booking.getGround().getPricePerHour());
        payment.setPaymentStatus(status);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return paymentRepository.save(payment);
    }

    private Report saveReport(User owner, Report.ReportType reportType) {
        Report report = new Report();
        report.setOwner(owner);
        report.setReportType(reportType);
        report.setReportData("{}");
        report.setStartDate(LocalDateTime.now().minusDays(7));
        report.setEndDate(LocalDateTime.now());
        return reportRepository.save(report);
    }

    private FutsalCompany saveCompany(User owner) {
        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName("Company " + UUID.randomUUID());
        company.setLocation("Kathmandu");
        return companyRepository.save(company);
    }

    private FutsalGround saveGround(FutsalCompany company) {
        FutsalGround ground = new FutsalGround();
        ground.setCompany(company);
        ground.setName("Ground " + UUID.randomUUID());
        ground.setSurfaceType("Indoor");
        ground.setPricePerHour(BigDecimal.valueOf(1500));
        return groundRepository.save(ground);
    }

    private TimeSlot saveSlot(FutsalGround ground, boolean isBooked) {
        TimeSlot slot = new TimeSlot();
        slot.setGround(ground);
        slot.setStartTime(LocalDateTime.now().plusDays(1));
        slot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        slot.setIsBooked(isBooked);
        return timeSlotRepository.save(slot);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.com";
    }

    private String uniquePhone() {
        String digits = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        return String.format("%1$-10.10s", digits).replace(' ', '0');
    }
}
