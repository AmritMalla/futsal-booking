package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
