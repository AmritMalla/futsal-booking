package com.amrit.futsal.api;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthBookingIntegrationTest {

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

    @Test
    void registerCreatesUserAndReturnsJwt() throws Exception {
        String email = uniqueEmail("register");

        String requestBody = """
                {
                  "name": "Phase Three User",
                  "email": "%s",
                  "password": "password123",
                  "phoneNumber": "%s",
                  "role": "USER"
                }
                """.formatted(email, uniquePhone());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"));

        User createdUser = userRepository.findByEmail(email).orElseThrow();
        assertThat(passwordEncoder.matches("password123", createdUser.getPasswordHash())).isTrue();
        assertThat(createdUser.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void loginReturnsJwtForExistingUser() throws Exception {
        String email = uniqueEmail("login");
        saveUser("Login User", email, "password123", User.Role.USER);

        String requestBody = """
                {
                  "email": "%s",
                  "password": "password123"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(username = "booking.user@example.com", roles = "USER")
    void createBookingCreatesBookingAndMarksSlotBooked() throws Exception {
        User bookingUser = saveUser("Booking User", "booking.user@example.com", "password123", User.Role.USER);
        FutsalGround ground = saveGround();
        TimeSlot slot = saveSlot(ground, false);

        String requestBody = """
                {
                  "groundId": "%s",
                  "slotId": "%s"
                }
                """.formatted(ground.getId(), slot.getId());

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(bookingUser.getId().toString()))
                .andExpect(jsonPath("$.groundId").value(ground.getId().toString()))
                .andExpect(jsonPath("$.slotId").value(slot.getId().toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        TimeSlot updatedSlot = timeSlotRepository.findById(slot.getId()).orElseThrow();
        assertThat(updatedSlot.getIsBooked()).isTrue();

        Booking createdBooking = bookingRepository.findBySlotId(slot.getId()).stream().findFirst().orElseThrow();
        assertThat(createdBooking.getUser().getId()).isEqualTo(bookingUser.getId());
        assertThat(createdBooking.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
    }

    @Test
    @WithMockUser(username = "booking.user@example.com", roles = "USER")
    void createBookingRejectsAlreadyBookedSlot() throws Exception {
        saveUser("Booking User", "booking.user@example.com", "password123", User.Role.USER);
        FutsalGround ground = saveGround();
        TimeSlot bookedSlot = saveSlot(ground, true);

        String requestBody = """
                {
                  "groundId": "%s",
                  "slotId": "%s"
                }
                """.formatted(ground.getId(), bookedSlot.getId());

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The selected time slot is already booked"));
    }

    private User saveUser(String name, String email, String rawPassword, User.Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPhoneNumber(uniquePhone());
        user.setRole(role);
        return userRepository.save(user);
    }

    private FutsalGround saveGround() {
        User owner = saveUser("Owner User", uniqueEmail("owner"), "password123", User.Role.OWNER);

        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName("Arena " + UUID.randomUUID());
        company.setLocation("Kathmandu");
        FutsalCompany savedCompany = companyRepository.save(company);

        FutsalGround ground = new FutsalGround();
        ground.setCompany(savedCompany);
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
