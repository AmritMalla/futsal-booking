package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserId(UUID userId);

    List<Booking> findByGroundId(UUID groundId);

    List<Booking> findBySlotId(UUID slotId);

    List<Booking> findByStatus(Booking.BookingStatus status);

    List<Booking> findByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.ground.company.owner.id = :ownerId")
    List<Booking> findByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT b FROM Booking b WHERE b.ground.company.owner.id = :ownerId " +
            "AND b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByOwnerIdAndDateRange(
            @Param("ownerId") UUID ownerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.ground.company.owner.id = :ownerId AND b.status = :status")
    List<Booking> findByOwnerIdAndStatus(
            @Param("ownerId") UUID ownerId,
            @Param("status") Booking.BookingStatus status);

    @Query("SELECT COUNT(DISTINCT b.user.id) FROM Booking b WHERE b.ground.company.owner.id = :ownerId")
    long countDistinctCustomersByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COUNT(DISTINCT b.user.id) FROM Booking b WHERE b.ground.company.owner.id = :ownerId " +
            "AND b.bookingDate BETWEEN :startDate AND :endDate")
    long countNewCustomersByOwnerIdAndDateRange(
            @Param("ownerId") UUID ownerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    long countByStatus(Booking.BookingStatus status);
}
