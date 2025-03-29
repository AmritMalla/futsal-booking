package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByFutsalCompany_FutsalId(Long futsalId); // Find reviews by futsal company ID

    List<Review> findByCustomer_UserId(Long customerId); // Find reviews by customer ID
}
