package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByGroundId(UUID groundId);
    
    List<Review> findByUserId(UUID userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.ground.id = :groundId")
    Double calculateAverageRatingForGround(@Param("groundId") UUID groundId);
}
