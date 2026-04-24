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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminCrudIntegrationTest {

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
    @WithMockUser(username = "admin.manage@example.com", roles = "ADMIN")
    void adminCanManageUsers() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Managed Owner",
                                  "email": "managed.owner@example.com",
                                  "password": "password123",
                                  "phoneNumber": "9800000001",
                                  "role": "OWNER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("managed.owner@example.com"))
                .andExpect(jsonPath("$.role").value("OWNER"));

        User managedUser = userRepository.findByEmail("managed.owner@example.com").orElseThrow();

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email").value(org.hamcrest.Matchers.hasItem("managed.owner@example.com")));

        mockMvc.perform(get("/api/v1/admin/owners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(org.hamcrest.Matchers.hasItem("managed.owner@example.com")));

        mockMvc.perform(put("/api/v1/admin/users/%s".formatted(managedUser.getId()))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Updated Owner",
                                  "email": "updated.owner@example.com",
                                  "phoneNumber": "9800000002"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Owner"))
                .andExpect(jsonPath("$.email").value("updated.owner@example.com"));

        mockMvc.perform(patch("/api/v1/admin/users/%s/role".formatted(managedUser.getId()))
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(get("/api/v1/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(org.hamcrest.Matchers.hasItem("updated.owner@example.com")));

        mockMvc.perform(delete("/api/v1/admin/users/%s".formatted(managedUser.getId())))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(managedUser.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin.manage@example.com", roles = "ADMIN")
    void adminCanManageCompaniesGroundsAndSlots() throws Exception {
        User owner = saveUser("Company Owner", "company.owner@example.com", User.Role.OWNER);

        mockMvc.perform(post("/api/v1/admin/companies")
                        .contentType("application/json")
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "name": "Arena Ops",
                                  "location": "Kathmandu"
                                }
                                """.formatted(owner.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Arena Ops"));

        FutsalCompany company = companyRepository.findByName("Arena Ops").orElseThrow();

        mockMvc.perform(put("/api/v1/admin/companies/%s".formatted(company.getId()))
                        .contentType("application/json")
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "name": "Arena Ops Updated",
                                  "location": "Lalitpur"
                                }
                                """.formatted(owner.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Arena Ops Updated"))
                .andExpect(jsonPath("$.location").value("Lalitpur"));

        mockMvc.perform(post("/api/v1/admin/grounds")
                        .contentType("application/json")
                        .content("""
                                {
                                  "companyId": "%s",
                                  "name": "Center Court",
                                  "surfaceType": "Indoor",
                                  "pricePerHour": 1800,
                                  "imageUrl": null
                                }
                                """.formatted(company.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Center Court"));

        FutsalGround ground = groundRepository.findByName("Center Court").orElseThrow();

        mockMvc.perform(put("/api/v1/admin/grounds/%s".formatted(ground.getId()))
                        .contentType("application/json")
                        .content("""
                                {
                                  "companyId": "%s",
                                  "name": "Center Court Updated",
                                  "surfaceType": "Outdoor",
                                  "pricePerHour": 2200,
                                  "imageUrl": null
                                }
                                """.formatted(company.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Center Court Updated"))
                .andExpect(jsonPath("$.surfaceType").value("Outdoor"))
                .andExpect(jsonPath("$.pricePerHour").value(2200));

        LocalDateTime startTime = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusHours(1);

        mockMvc.perform(post("/api/v1/admin/timeslots")
                        .contentType("application/json")
                        .content("""
                                {
                                  "groundId": "%s",
                                  "startTime": "%s",
                                  "endTime": "%s"
                                }
                                """.formatted(ground.getId(), startTime, endTime)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groundId").value(ground.getId().toString()))
                .andExpect(jsonPath("$.isBooked").value(false));

        TimeSlot slot = timeSlotRepository.findByGroundId(ground.getId()).stream().findFirst().orElseThrow();
        LocalDateTime updatedStart = startTime.plusHours(2);
        LocalDateTime updatedEnd = endTime.plusHours(2);

        mockMvc.perform(get("/api/v1/admin/timeslots")
                        .param("groundId", ground.getId().toString())
                        .param("isBooked", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(slot.getId().toString()));

        mockMvc.perform(put("/api/v1/admin/timeslots/%s".formatted(slot.getId()))
                        .contentType("application/json")
                        .content("""
                                {
                                  "groundId": "%s",
                                  "startTime": "%s",
                                  "endTime": "%s"
                                }
                                """.formatted(ground.getId(), updatedStart, updatedEnd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(slot.getId().toString()))
                .andExpect(jsonPath("$.startTime[0]").value(updatedStart.getYear()))
                .andExpect(jsonPath("$.startTime[1]").value(updatedStart.getMonthValue()))
                .andExpect(jsonPath("$.startTime[2]").value(updatedStart.getDayOfMonth()))
                .andExpect(jsonPath("$.startTime[3]").value(updatedStart.getHour()));

        mockMvc.perform(delete("/api/v1/admin/timeslots/%s".formatted(slot.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/admin/grounds/%s".formatted(ground.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/admin/companies/%s".formatted(company.getId())))
                .andExpect(status().isNoContent());

        assertThat(timeSlotRepository.findById(slot.getId())).isEmpty();
        assertThat(groundRepository.findById(ground.getId())).isEmpty();
        assertThat(companyRepository.findById(company.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin.manage@example.com", roles = "ADMIN")
    void adminCanInspectAndUpdateBookingsAndPayments() throws Exception {
        User owner = saveUser("Ops Owner", uniqueEmail("owner"), User.Role.OWNER);
        User customer = saveUser("Ops Customer", uniqueEmail("customer"), User.Role.USER);
        FutsalCompany company = saveCompany(owner, "Ops Company");
        FutsalGround ground = saveGround(company, "Ops Ground", BigDecimal.valueOf(2000));
        TimeSlot originalSlot = saveSlot(ground, LocalDateTime.now().plusDays(3).withHour(17), true);
        TimeSlot replacementSlot = saveSlot(ground, LocalDateTime.now().plusDays(3).withHour(19), false);
        Booking booking = saveBooking(customer, ground, originalSlot, Booking.BookingStatus.CONFIRMED);
        Payment payment = savePayment(customer, booking, BigDecimal.valueOf(2000), Payment.PaymentStatus.PENDING, "TXN-ORIGINAL");

        mockMvc.perform(get("/api/v1/admin/bookings")
                        .param("userId", customer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getId().toString()))
                .andExpect(jsonPath("$[0].paymentStatus").value("PENDING"));

        mockMvc.perform(get("/api/v1/admin/bookings/%s".formatted(booking.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()))
                .andExpect(jsonPath("$.groundName").value("Ops Ground"));

        mockMvc.perform(put("/api/v1/admin/bookings/%s".formatted(booking.getId()))
                        .contentType("application/json")
                        .content("""
                                {
                                  "groundId": "%s",
                                  "slotId": "%s"
                                }
                                """.formatted(ground.getId(), replacementSlot.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotId").value(replacementSlot.getId().toString()));

        mockMvc.perform(get("/api/v1/admin/payments")
                        .param("userId", customer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(payment.getId().toString()))
                .andExpect(jsonPath("$[0].paymentStatus").value("PENDING"));

        mockMvc.perform(get("/api/v1/admin/payments/%s".formatted(payment.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-ORIGINAL"));

        mockMvc.perform(put("/api/v1/admin/payments/%s".formatted(payment.getId()))
                        .contentType("application/json")
                        .content("""
                                {
                                  "bookingId": "%s",
                                  "amount": 2000,
                                  "transactionId": "TXN-UPDATED"
                                }
                                """.formatted(booking.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-UPDATED"))
                .andExpect(jsonPath("$.amount").value(2000));

        mockMvc.perform(delete("/api/v1/admin/bookings/%s".formatted(booking.getId())))
                .andExpect(status().isNoContent());

        Booking reloadedBooking = bookingRepository.findById(booking.getId()).orElse(null);
        TimeSlot updatedOriginalSlot = timeSlotRepository.findById(originalSlot.getId()).orElseThrow();
        TimeSlot updatedReplacementSlot = timeSlotRepository.findById(replacementSlot.getId()).orElseThrow();
        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();

        assertThat(reloadedBooking).isNull();
        assertThat(updatedOriginalSlot.getIsBooked()).isFalse();
        assertThat(updatedReplacementSlot.getIsBooked()).isTrue();
        assertThat(updatedPayment.getTransactionId()).isEqualTo("TXN-UPDATED");
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

    private Booking saveBooking(User user, FutsalGround ground, TimeSlot slot, Booking.BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setGround(ground);
        booking.setSlot(slot);
        booking.setBookingDate(LocalDateTime.now().minusHours(2));
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private Payment savePayment(User user,
                                Booking booking,
                                BigDecimal amount,
                                Payment.PaymentStatus status,
                                String transactionId) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentStatus(status);
        payment.setTransactionId(transactionId);
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
