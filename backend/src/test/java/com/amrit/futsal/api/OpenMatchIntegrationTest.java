package com.amrit.futsal.api;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.OpenMatch;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.OpenMatchRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OpenMatchIntegrationTest {

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
    private OpenMatchRepository openMatchRepository;

    @Test
    void publicUsersCanBrowseOpenMatches() throws Exception {
        User host = saveUser("Host User", uniqueEmail("host"), User.Role.USER);
        Booking booking = saveBooking(host);
        OpenMatch match = saveOpenMatch(booking, host, 4);

        mockMvc.perform(get("/api/v1/open-matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(match.getId().toString()))
                .andExpect(jsonPath("$[0].groundId").value(booking.getGround().getId().toString()))
                .andExpect(jsonPath("$[0].currentPlayerCount").value(1));

        mockMvc.perform(get("/api/v1/open-matches/ground/%s".formatted(booking.getGround().getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(match.getId().toString()));
    }

    @Test
    @WithMockUser(username = "host.match@example.com", roles = "USER")
    void bookingOwnerCanCreateOpenMatch() throws Exception {
        User host = saveUser("Host User", "host.match@example.com", User.Role.USER);
        Booking booking = saveBooking(host);

        mockMvc.perform(post("/api/v1/open-matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookingId": "%s",
                                  "title": "Friday 5v5 Pickup",
                                  "skillLevel": "INTERMEDIATE",
                                  "desiredPlayerCount": 6,
                                  "notes": "Need a few midfielders."
                                }
                                """.formatted(booking.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(booking.getId().toString()))
                .andExpect(jsonPath("$.hostUserId").value(host.getId().toString()))
                .andExpect(jsonPath("$.currentPlayerCount").value(1))
                .andExpect(jsonPath("$.openSpots").value(5))
                .andExpect(jsonPath("$.skillLevel").value("INTERMEDIATE"));

        OpenMatch createdMatch = openMatchRepository.findByBookingId(booking.getId()).orElseThrow();
        assertThat(createdMatch.getHost().getId()).isEqualTo(host.getId());
        assertThat(createdMatch.getStatus()).isEqualTo(OpenMatch.OpenMatchStatus.OPEN);
    }

    @Test
    @WithMockUser(username = "joiner.match@example.com", roles = "USER")
    void signedInUsersCanJoinAndLeaveOpenMatches() throws Exception {
        User host = saveUser("Host User", uniqueEmail("host"), User.Role.USER);
        User joiner = saveUser("Joiner User", "joiner.match@example.com", User.Role.USER);
        Booking booking = saveBooking(host);
        OpenMatch match = saveOpenMatch(booking, host, 3);

        mockMvc.perform(post("/api/v1/open-matches/%s/join".formatted(match.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerCount").value(2))
                .andExpect(jsonPath("$.participantUserIds[0]").value(joiner.getId().toString()));

        OpenMatch joinedMatch = openMatchRepository.findById(match.getId()).orElseThrow();
        assertThat(joinedMatch.getParticipants()).extracting(User::getId).contains(joiner.getId());

        mockMvc.perform(post("/api/v1/open-matches/%s/leave".formatted(match.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerCount").value(1));

        OpenMatch leftMatch = openMatchRepository.findById(match.getId()).orElseThrow();
        assertThat(leftMatch.getParticipants()).extracting(User::getId).doesNotContain(joiner.getId());
    }

    @Test
    @WithMockUser(username = "host.match@example.com", roles = "USER")
    void hostsCanCancelTheirOpenMatches() throws Exception {
        User host = saveUser("Host User", "host.match@example.com", User.Role.USER);
        Booking booking = saveBooking(host);
        OpenMatch match = saveOpenMatch(booking, host, 4);

        mockMvc.perform(delete("/api/v1/open-matches/%s".formatted(match.getId())))
                .andExpect(status().isNoContent());

        OpenMatch cancelledMatch = openMatchRepository.findById(match.getId()).orElseThrow();
        assertThat(cancelledMatch.getStatus()).isEqualTo(OpenMatch.OpenMatchStatus.CANCELLED);
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

    private Booking saveBooking(User bookingUser) {
        User owner = saveUser("Owner User", uniqueEmail("owner"), User.Role.OWNER);

        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName("Arena " + UUID.randomUUID());
        company.setLocation("Kathmandu");
        company = companyRepository.save(company);

        FutsalGround ground = new FutsalGround();
        ground.setCompany(company);
        ground.setName("Ground " + UUID.randomUUID());
        ground.setSurfaceType("Indoor");
        ground.setPricePerHour(BigDecimal.valueOf(1500));
        ground = groundRepository.save(ground);

        TimeSlot slot = new TimeSlot();
        slot.setGround(ground);
        slot.setStartTime(LocalDateTime.now().plusDays(2).withHour(18).withMinute(0));
        slot.setEndTime(slot.getStartTime().plusHours(1));
        slot.setIsBooked(true);
        slot = timeSlotRepository.save(slot);

        Booking booking = new Booking();
        booking.setUser(bookingUser);
        booking.setGround(ground);
        booking.setSlot(slot);
        booking.setBookingDate(LocalDateTime.now().minusHours(1));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    private OpenMatch saveOpenMatch(Booking booking, User host, int desiredPlayerCount) {
        OpenMatch match = new OpenMatch();
        match.setBooking(booking);
        match.setHost(host);
        match.setTitle("Open Match " + UUID.randomUUID());
        match.setSkillLevel(OpenMatch.SkillLevel.ANY);
        match.setDesiredPlayerCount(desiredPlayerCount);
        match.setNotes("Bring a light and dark shirt.");
        match.setStatus(OpenMatch.OpenMatchStatus.OPEN);
        return openMatchRepository.save(match);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.com";
    }

    private String uniquePhone() {
        String digits = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        return String.format("%1$-10.10s", digits).replace(' ', '0');
    }
}
