package com.amrit.futsal.repository;

import com.amrit.futsal.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    List<TimeSlot> findByGroundId(UUID groundId);
    
    List<TimeSlot> findByIsBooked(Boolean isBooked);
    
    List<TimeSlot> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT t FROM TimeSlot t WHERE t.ground.id = :groundId AND t.startTime BETWEEN :start AND :end")
    List<TimeSlot> findTimeSlotsByGroundAndDateRange(@Param("groundId") UUID groundId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM TimeSlot t WHERE t.ground.id = :groundId AND t.isBooked = false")
    List<TimeSlot> findAvailableSlotsByGround(@Param("groundId") UUID groundId);

    @Query("SELECT COUNT(t) FROM TimeSlot t WHERE t.ground.id = :groundId AND t.startTime < :endTime AND t.endTime > :startTime")
    long countOverlappingSlots(@Param("groundId") UUID groundId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
}
