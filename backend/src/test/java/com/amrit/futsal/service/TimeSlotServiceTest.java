package com.amrit.futsal.service;

import com.amrit.futsal.dto.TimeSlotRequest;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.TimeSlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private FutsalGroundRepository futsalGroundRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;

    @Test
    void createTimeSlotRejectsDuplicateTimeRange() {
        UUID groundId = UUID.randomUUID();
        TimeSlotRequest request = new TimeSlotRequest(
                groundId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1)
        );

        when(timeSlotRepository.existsByGroundIdAndStartTimeAndEndTime(
                request.getGroundId(),
                request.getStartTime(),
                request.getEndTime()
        )).thenReturn(true);

        assertThrows(BadRequestException.class, () -> timeSlotService.createTimeSlot(request));
    }

    @Test
    void deleteTimeSlotRejectsBookedSlot() {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(UUID.randomUUID());
        timeSlot.setGround(new FutsalGround());
        timeSlot.setIsBooked(true);

        when(timeSlotRepository.findById(timeSlot.getId())).thenReturn(Optional.of(timeSlot));

        assertThrows(BadRequestException.class, () -> timeSlotService.deleteTimeSlot(timeSlot.getId()));
    }
}
