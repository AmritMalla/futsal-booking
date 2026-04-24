package com.amrit.futsal.api;

import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.User;
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
class OwnerResourceIntegrationTest {

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

    @Test
    @WithMockUser(username = "owner.ground@example.com", roles = "OWNER")
    void createGroundAllowsCompanyOwner() throws Exception {
        User owner = saveUser("Owner Ground", "owner.ground@example.com", User.Role.OWNER);
        FutsalCompany company = saveCompany(owner);

        String requestBody = """
                {
                  "companyId": "%s",
                  "name": "Owner Ground %s",
                  "surfaceType": "Indoor",
                  "pricePerHour": 1800
                }
                """.formatted(company.getId(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/grounds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(company.getId().toString()))
                .andExpect(jsonPath("$.surfaceType").value("Indoor"))
                .andExpect(jsonPath("$.pricePerHour").value(1800));
    }

    @Test
    @WithMockUser(username = "plain.ground.user@example.com", roles = "USER")
    void createGroundRejectsRegularUser() throws Exception {
        saveUser("Plain User", "plain.ground.user@example.com", User.Role.USER);
        User owner = saveUser("Owner User", uniqueEmail("owner"), User.Role.OWNER);
        FutsalCompany company = saveCompany(owner);

        String requestBody = """
                {
                  "companyId": "%s",
                  "name": "Unauthorized Ground",
                  "surfaceType": "Indoor",
                  "pricePerHour": 1500
                }
                """.formatted(company.getId());

        mockMvc.perform(post("/api/v1/grounds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner.slot@example.com", roles = "OWNER")
    void createTimeSlotAllowsGroundOwner() throws Exception {
        User owner = saveUser("Owner Slot", "owner.slot@example.com", User.Role.OWNER);
        FutsalGround ground = saveGround(owner);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        String requestBody = """
                {
                  "groundId": "%s",
                  "startTime": "%s",
                  "endTime": "%s"
                }
                """.formatted(ground.getId(), start, end);

        mockMvc.perform(post("/api/v1/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groundId").value(ground.getId().toString()))
                .andExpect(jsonPath("$.isBooked").value(false));

        assertThat(timeSlotRepository.findByGroundId(ground.getId())).hasSize(1);
    }

    @Test
    @WithMockUser(username = "other.owner@example.com", roles = "OWNER")
    void createTimeSlotRejectsDifferentOwner() throws Exception {
        saveUser("Other Owner", "other.owner@example.com", User.Role.OWNER);
        User actualOwner = saveUser("Actual Owner", uniqueEmail("actual-owner"), User.Role.OWNER);
        FutsalGround ground = saveGround(actualOwner);

        LocalDateTime start = LocalDateTime.now().plusDays(3).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        String requestBody = """
                {
                  "groundId": "%s",
                  "startTime": "%s",
                  "endTime": "%s"
                }
                """.formatted(ground.getId(), start, end);

        mockMvc.perform(post("/api/v1/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
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

    private FutsalCompany saveCompany(User owner) {
        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName("Company " + UUID.randomUUID());
        company.setLocation("Kathmandu");
        return companyRepository.save(company);
    }

    private FutsalGround saveGround(User owner) {
        FutsalCompany company = saveCompany(owner);

        FutsalGround ground = new FutsalGround();
        ground.setCompany(company);
        ground.setName("Ground " + UUID.randomUUID());
        ground.setSurfaceType("Indoor");
        ground.setPricePerHour(BigDecimal.valueOf(1500));
        return groundRepository.save(ground);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.com";
    }

    private String uniquePhone() {
        String digits = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        return String.format("%1$-10.10s", digits).replace(' ', '0');
    }
}
