package com.amrit.futsal.repository;

import com.amrit.futsal.entity.OpenMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpenMatchRepository extends JpaRepository<OpenMatch, UUID> {

    boolean existsByBookingId(UUID bookingId);

    Optional<OpenMatch> findByBookingId(UUID bookingId);

    @Query("""
            SELECT DISTINCT m FROM OpenMatch m
            LEFT JOIN FETCH m.participants
            WHERE m.status <> com.amrit.futsal.entity.OpenMatch$OpenMatchStatus.CANCELLED
              AND m.booking.status = com.amrit.futsal.entity.Booking$BookingStatus.CONFIRMED
              AND m.booking.slot.startTime > :now
            """)
    List<OpenMatch> findDiscoverableMatches(@Param("now") LocalDateTime now);

    @Query("""
            SELECT DISTINCT m FROM OpenMatch m
            LEFT JOIN FETCH m.participants
            WHERE m.booking.ground.id = :groundId
              AND m.status <> com.amrit.futsal.entity.OpenMatch$OpenMatchStatus.CANCELLED
              AND m.booking.status = com.amrit.futsal.entity.Booking$BookingStatus.CONFIRMED
              AND m.booking.slot.startTime > :now
            """)
    List<OpenMatch> findDiscoverableMatchesByGroundId(@Param("groundId") UUID groundId,
                                                      @Param("now") LocalDateTime now);

    @Query("""
            SELECT DISTINCT m FROM OpenMatch m
            LEFT JOIN FETCH m.participants
            WHERE m.host.id = :userId OR EXISTS (
                SELECT participant FROM m.participants participant WHERE participant.id = :userId
            )
            """)
    List<OpenMatch> findUserRelatedMatches(@Param("userId") UUID userId);
}
