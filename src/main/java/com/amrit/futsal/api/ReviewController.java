package com.amrit.futsal.api;

import com.amrit.futsal.entity.Review;
import com.amrit.futsal.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReviewById(@PathVariable UUID reviewId) {
        return reviewService.getReviewById(reviewId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/ground/{groundId}")
    public ResponseEntity<List<Review>> getReviewsByGroundId(@PathVariable UUID groundId) {
        return ResponseEntity.ok(reviewService.getReviewsByGroundId(groundId));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
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
    public ResponseEntity<Review> updateReview(@PathVariable UUID reviewId, @RequestBody Review review) {
        if (!reviewId.equals(review.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
