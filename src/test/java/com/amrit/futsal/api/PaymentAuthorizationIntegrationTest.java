package com.amrit.futsal.api;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.PaymentRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentAuthorizationIntegrationTest {

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

    @Test
    @WithMockUser(username = "payment.user@example.com", roles = "USER")
    void processPaymentCreatesSuccessfulPayment() throws Exception {
        User user = saveUser("Payment User", "payment.user@example.com", User.Role.USER);
        Booking booking = saveBooking(user, Booking.BookingStatus.CONFIRMED, true);

        String requestBody = """
                {
                  "bookingId": "%s",
                  "amount": 1200,
                  "transactionId": "TXN-INTEGRATION-001"
                }
                """.formatted(booking.getId());

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(booking.getId().toString()))
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("TXN-INTEGRATION-001"));

        Payment savedPayment = paymentRepository.findByBookingId(booking.getId()).stream().findFirst().orElseThrow();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(Payment.PaymentStatus.SUCCESS);
    }

    @Test
    @WithMockUser(username = "payment.user@example.com", roles = "USER")
    void processPaymentRejectsAmountMismatch() throws Exception {
        User user = saveUser("Payment User", "payment.user@example.com", User.Role.USER);
        Booking booking = saveBooking(user, Booking.BookingStatus.CONFIRMED, true);

        String requestBody = """
                {
                  "bookingId": "%s",
                  "amount": 999,
                  "transactionId": "TXN-INTEGRATION-002"
                }
                """.formatted(booking.getId());

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment amount must match the booking price"));
    }

    @Test
    @WithMockUser(username = "payment.user@example.com", roles = "USER")
    void refundPaymentCancelsBookingAndFreesSlot() throws Exception {
        User user = saveUser("Payment User", "payment.user@example.com", User.Role.USER);
        Booking booking = saveBooking(user, Booking.BookingStatus.CONFIRMED, true);
        Payment payment = savePayment(booking, user, Payment.PaymentStatus.SUCCESS, BigDecimal.valueOf(1200));

        mockMvc.perform(post("/api/v1/payments/%s/refund".formatted(payment.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"));

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        TimeSlot updatedSlot = timeSlotRepository.findById(booking.getSlot().getId()).orElseThrow();

        assertThat(updatedBooking.getStatus()).isEqualTo(Booking.BookingStatus.CANCELLED);
        assertThat(updatedSlot.getIsBooked()).isFalse();
    }

    @Test
    @WithMockUser(username = "owner.protected@example.com", roles = "OWNER")
    void adminUsersEndpointRejectsOwner() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "plain.user@example.com", roles = "USER")
    void createCompanyRejectsRegularUser() throws Exception {
        saveUser("Plain User", "plain.user@example.com", User.Role.USER);

        String requestBody = """
                {
                  "name": "Unauthorized Arena",
                  "location": "Bhaktapur"
                }
                """;

        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner.user@example.com", roles = "OWNER")
    void createCompanyAllowsOwner() throws Exception {
        User owner = saveUser("Owner User", "owner.user@example.com", User.Role.OWNER);

        String requestBody = """
                {
                  "name": "Owner Arena %s",
                  "location": "Lalitpur"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(owner.getId().toString()))
                .andExpect(jsonPath("$.location").value("Lalitpur"));
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

        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName("Company " + UUID.randomUUID());
        company.setLocation("Kathmandu");
        FutsalCompany savedCompany = companyRepository.save(company);

        FutsalGround ground = new FutsalGround();
        ground.setCompany(savedCompany);
        ground.setName("Ground " + UUID.randomUUID());
        ground.setSurfaceType("Indoor");
        ground.setPricePerHour(BigDecimal.valueOf(1200));
        FutsalGround savedGround = groundRepository.save(ground);

        TimeSlot slot = new TimeSlot();
        slot.setGround(savedGround);
        slot.setStartTime(LocalDateTime.now().plusDays(1));
        slot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        slot.setIsBooked(slotBooked);
        TimeSlot savedSlot = timeSlotRepository.save(slot);

        Booking booking = new Booking();
        booking.setUser(bookingUser);
        booking.setGround(savedGround);
        booking.setSlot(savedSlot);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private Payment savePayment(Booking booking, User user, Payment.PaymentStatus status, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentStatus(status);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return paymentRepository.save(payment);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.com";
    }

    private String uniquePhone() {
        String digits = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        return String.format("%1$-10.10s", digits).replace(' ', '0');
    }
}
