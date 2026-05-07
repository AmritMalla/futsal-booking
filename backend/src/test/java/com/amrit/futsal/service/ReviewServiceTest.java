package com.amrit.futsal.service;

import com.amrit.futsal.dto.ReviewRequest;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Review;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private FutsalGroundRepository groundRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewUsesAuthenticatedUserAndGroundFromRequest() {
        User currentUser = buildUser();
        FutsalGround ground = buildGround();
        ReviewRequest request = new ReviewRequest(ground.getId(), 5, "Great ground and smooth booking flow.");

        when(reviewRepository.findByUserIdAndGroundId(currentUser.getId(), ground.getId())).thenReturn(Optional.empty());
        when(groundRepository.findById(ground.getId())).thenReturn(Optional.of(ground));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review createdReview = reviewService.createReview(currentUser, request);

        assertEquals(currentUser, createdReview.getUser());
        assertEquals(ground, createdReview.getGround());
        assertEquals(request.getRating(), createdReview.getRating());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        assertEquals(currentUser, reviewCaptor.getValue().getUser());
    }

    @Test
    void createReviewRejectsDuplicateReviewForSameGround() {
        User currentUser = buildUser();
        FutsalGround ground = buildGround();
        Review existingReview = new Review();
        existingReview.setId(UUID.randomUUID());
        existingReview.setUser(currentUser);
        existingReview.setGround(ground);

        ReviewRequest request = new ReviewRequest(ground.getId(), 4, "Still a solid experience overall.");

        when(reviewRepository.findByUserIdAndGroundId(currentUser.getId(), ground.getId()))
                .thenReturn(Optional.of(existingReview));

        assertThrows(BadRequestException.class, () -> reviewService.createReview(currentUser, request));
    }

    @Test
    void updateReviewRejectsChangingGround() {
        FutsalGround existingGround = buildGround();
        FutsalGround differentGround = buildGround();
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setGround(existingGround);

        ReviewRequest request = new ReviewRequest(differentGround.getId(), 3, "Updated text");

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        assertThrows(BadRequestException.class, () -> reviewService.updateReview(review.getId(), request));
    }

    private User buildUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Reviewer");
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setRole(User.Role.USER);
        return user;
    }

    private FutsalGround buildGround() {
        User owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(User.Role.OWNER);

        FutsalCompany company = new FutsalCompany();
        company.setId(UUID.randomUUID());
        company.setOwner(owner);

        FutsalGround ground = new FutsalGround();
        ground.setId(UUID.randomUUID());
        ground.setCompany(company);
        ground.setName("Review Test Ground");
        ground.setPricePerHour(BigDecimal.valueOf(1600));
        return ground;
    }
}
