package com.amrit.futsal.api;

import com.amrit.futsal.dto.ReviewRequest;
import com.amrit.futsal.dto.ReviewResponse;
import com.amrit.futsal.entity.Review;
import com.amrit.futsal.service.AuthenticatedUserService;
import com.amrit.futsal.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public ReviewController(ReviewService reviewService,
                            AuthenticatedUserService authenticatedUserService) {
        this.reviewService = reviewService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.createReview(authenticatedUserService.getCurrentUser(), request);
        return ResponseEntity.ok(ReviewResponse.fromEntity(review));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable UUID reviewId) {
        return reviewService.getReviewById(reviewId)
                .map(ReviewResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/ground/{groundId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByGroundId(@PathVariable UUID groundId) {
        return ResponseEntity.ok(reviewService.getReviewsByGroundId(groundId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList()));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable UUID userId) {
        authenticatedUserService.requireCurrentUserOrAdmin(userId);
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList()));
    }
    
    @GetMapping("/ground/{groundId}/rating")
    public ResponseEntity<Double> getAverageRatingForGround(@PathVariable UUID groundId) {
        Double averageRating = reviewService.getAverageRatingForGround(groundId);
        if (averageRating != null) {
            return ResponseEntity.ok(averageRating);
        }
        return ResponseEntity.ok(0.0); // Return 0 if no ratings yet
    }
    
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable UUID reviewId,
                                                       @Valid @RequestBody ReviewRequest request) {
        authenticatedUserService.requireReviewOwnerOrAdmin(reviewId);
        return ResponseEntity.ok(ReviewResponse.fromEntity(reviewService.updateReview(reviewId, request)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        authenticatedUserService.requireReviewOwnerOrAdmin(reviewId);
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
