package com.amrit.futsal.repository;

import com.amrit.futsal.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByGround_GroundId(Long groundId); // Find slots by ground ID

    List<TimeSlot> findByStatus(TimeSlot.Status status); // Find slots by status

    List<TimeSlot> findByStartTimeBetween(LocalDateTime start, LocalDateTime end); // Find slots within a time range

    @Query("SELECT t FROM TimeSlot t WHERE t.ground.groundId = :groundId AND t.startTime BETWEEN :start AND :end")
    List<TimeSlot> findTimeSlotsByGroundAndStartTimeBetween(@Param("groundId") Long groundId,
                                                            @Param("start") LocalDateTime start,
                                                            @Param("end") LocalDateTime end);


}
