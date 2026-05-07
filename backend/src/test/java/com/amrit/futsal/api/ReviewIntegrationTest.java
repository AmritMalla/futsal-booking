package com.amrit.futsal.api;

import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Review;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.ReviewRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewIntegrationTest {

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
    private ReviewRepository reviewRepository;

    @Test
    @WithMockUser(username = "review.user@example.com", roles = "USER")
    void createReviewUsesAuthenticatedUser() throws Exception {
        User reviewer = saveUser("Reviewer", "review.user@example.com", User.Role.USER);
        FutsalGround ground = saveGround();

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groundId": "%s",
                                  "rating": 5,
                                  "reviewText": "Great futsal experience."
                                }
                                """.formatted(ground.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(reviewer.getId().toString()))
                .andExpect(jsonPath("$.groundId").value(ground.getId().toString()))
                .andExpect(jsonPath("$.rating").value(5));

        Review savedReview = reviewRepository.findByUserIdAndGroundId(reviewer.getId(), ground.getId()).orElseThrow();
        assertThat(savedReview.getUser().getId()).isEqualTo(reviewer.getId());
    }

    @Test
    @WithMockUser(username = "review.user@example.com", roles = "USER")
    void createReviewRejectsDuplicateReviewForSameGround() throws Exception {
        User reviewer = saveUser("Reviewer", "review.user@example.com", User.Role.USER);
        FutsalGround ground = saveGround();
        saveReview(reviewer, ground, 4, "Already reviewed");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groundId": "%s",
                                  "rating": 5,
                                  "reviewText": "Trying again."
                                }
                                """.formatted(ground.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You have already reviewed this ground"));
    }

    @Test
    @WithMockUser(username = "review.user@example.com", roles = "USER")
    void userCanUpdateOwnReview() throws Exception {
        User reviewer = saveUser("Reviewer", "review.user@example.com", User.Role.USER);
        FutsalGround ground = saveGround();
        Review review = saveReview(reviewer, ground, 3, "Okay");

        mockMvc.perform(put("/api/v1/reviews/%s".formatted(review.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groundId": "%s",
                                  "rating": 4,
                                  "reviewText": "Actually pretty good."
                                }
                                """.formatted(ground.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId().toString()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.reviewText").value("Actually pretty good."));

        Review updatedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(updatedReview.getRating()).isEqualTo(4);
    }

    @Test
    @WithMockUser(username = "other.user@example.com", roles = "USER")
    void differentUserCannotUpdateAnotherUsersReview() throws Exception {
        User reviewOwner = saveUser("Review Owner", uniqueEmail("review-owner"), User.Role.USER);
        User otherUser = saveUser("Other User", "other.user@example.com", User.Role.USER);
        FutsalGround ground = saveGround();
        Review review = saveReview(reviewOwner, ground, 4, "Owner review");

        mockMvc.perform(put("/api/v1/reviews/%s".formatted(review.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groundId": "%s",
                                  "rating": 1,
                                  "reviewText": "Hijacked."
                                }
                                """.formatted(ground.getId())))
                .andExpect(status().isForbidden());

        Review unchangedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertThat(unchangedReview.getRating()).isEqualTo(4);
        assertThat(otherUser.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    @WithMockUser(username = "admin.review@example.com", roles = "ADMIN")
    void adminCanDeleteAnyReview() throws Exception {
        saveUser("Admin User", "admin.review@example.com", User.Role.ADMIN);
        User reviewOwner = saveUser("Review Owner", uniqueEmail("review-owner"), User.Role.USER);
        FutsalGround ground = saveGround();
        Review review = saveReview(reviewOwner, ground, 5, "Excellent");

        mockMvc.perform(delete("/api/v1/reviews/%s".formatted(review.getId())))
                .andExpect(status().isNoContent());

        assertThat(reviewRepository.findById(review.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "review.user@example.com", roles = "USER")
    void userReviewListIsScopedToSelfUnlessAdmin() throws Exception {
        User currentUser = saveUser("Reviewer", "review.user@example.com", User.Role.USER);
        User otherUser = saveUser("Other Reviewer", uniqueEmail("other-reviewer"), User.Role.USER);
        FutsalGround ground = saveGround();
        saveReview(currentUser, ground, 5, "Mine");

        mockMvc.perform(get("/api/v1/reviews/user/%s".formatted(currentUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(currentUser.getId().toString()));

        mockMvc.perform(get("/api/v1/reviews/user/%s".formatted(otherUser.getId())))
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

    private FutsalGround saveGround() {
        User owner = saveUser("Ground Owner", uniqueEmail("owner"), User.Role.OWNER);

        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName("Company " + UUID.randomUUID());
        company.setLocation("Kathmandu");
        company = companyRepository.save(company);

        FutsalGround ground = new FutsalGround();
        ground.setCompany(company);
        ground.setName("Ground " + UUID.randomUUID());
        ground.setSurfaceType("Indoor");
        ground.setPricePerHour(BigDecimal.valueOf(1500));
        return groundRepository.save(ground);
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
