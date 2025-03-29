package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomer_UserId(Long customerId); // Find bookings by customer ID

    List<Booking> findByTimeSlot_SlotId(Long slotId); // Find bookings by time slot ID
}
