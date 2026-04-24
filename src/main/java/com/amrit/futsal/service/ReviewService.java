package com.amrit.futsal.service;

import com.amrit.futsal.dto.ReviewRequest;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Review;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FutsalGroundRepository groundRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         FutsalGroundRepository groundRepository) {
        this.reviewRepository = reviewRepository;
        this.groundRepository = groundRepository;
    }

    public Review createReview(User currentUser, ReviewRequest request) {
        if (reviewRepository.findByUserIdAndGroundId(currentUser.getId(), request.getGroundId()).isPresent()) {
            throw new BadRequestException("You have already reviewed this ground");
        }

        FutsalGround ground = groundRepository.findById(request.getGroundId())
                .orElseThrow(() -> new ResourceNotFoundException("Ground", "id", request.getGroundId()));

        Review review = new Review();
        review.setUser(currentUser);
        review.setGround(ground);
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        return reviewRepository.save(review);
    }

    public Optional<Review> getReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public List<Review> getReviewsByGroundId(UUID groundId) {
        return reviewRepository.findByGroundId(groundId);
    }
    
    public List<Review> getReviewsByUserId(UUID userId) {
        return reviewRepository.findByUserId(userId);
    }
    
    public Double getAverageRatingForGround(UUID groundId) {
        return reviewRepository.calculateAverageRatingForGround(groundId);
    }

    public Review updateReview(UUID reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getGround().getId().equals(request.getGroundId())) {
            throw new BadRequestException("Review ground cannot be changed");
        }

        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        return reviewRepository.save(review);
    }

    public void deleteReview(UUID reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
