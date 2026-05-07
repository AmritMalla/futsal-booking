package com.amrit.futsal.dto;

import com.amrit.futsal.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private UUID groundId;
    private String groundName;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUser().getId());
        response.setUserName(review.getUser().getName());
        response.setGroundId(review.getGround().getId());
        response.setGroundName(review.getGround().getName());
        response.setRating(review.getRating());
        response.setReviewText(review.getReviewText());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
