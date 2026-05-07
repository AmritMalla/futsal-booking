package com.amrit.futsal.service;

import com.amrit.futsal.dto.TimeSlotRequest;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final FutsalGroundRepository futsalGroundRepository;

    @Autowired
    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                           FutsalGroundRepository futsalGroundRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.futsalGroundRepository = futsalGroundRepository;
    }

    public TimeSlot createTimeSlot(TimeSlotRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());
        if (timeSlotRepository.existsByGroundIdAndStartTimeAndEndTime(
                request.getGroundId(),
                request.getStartTime(),
                request.getEndTime()
        )) {
            throw new BadRequestException("A time slot already exists for the selected ground and time range");
        }

        FutsalGround ground = futsalGroundRepository.findById(request.getGroundId())
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "id", request.getGroundId()));

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setGround(ground);
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setIsBooked(false);

        return timeSlotRepository.save(timeSlot);
    }

    public Optional<TimeSlot> getTimeSlotById(UUID slotId) {
        return timeSlotRepository.findById(slotId);
    }

    public List<TimeSlot> getTimeSlotsByGroundId(UUID groundId) {
        return timeSlotRepository.findByGroundId(groundId);
    }
    
    public List<TimeSlot> getAvailableTimeSlots(UUID groundId) {
        return timeSlotRepository.findAvailableSlotsByGround(groundId);
    }
    
    public List<TimeSlot> getTimeSlotsByDateRange(UUID groundId, LocalDateTime start, LocalDateTime end) {
        return timeSlotRepository.findTimeSlotsByGroundAndDateRange(groundId, start, end);
    }

    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    public TimeSlot updateTimeSlot(UUID slotId, TimeSlotRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));

        if (!timeSlot.getGround().getId().equals(request.getGroundId())) {
            throw new BadRequestException("Changing a time slot's ground is not supported");
        }
        if (Boolean.TRUE.equals(timeSlot.getIsBooked())) {
            throw new BadRequestException("Booked time slots cannot be updated");
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());
        if (timeSlotRepository.existsByGroundIdAndStartTimeAndEndTimeAndIdNot(
                request.getGroundId(),
                request.getStartTime(),
                request.getEndTime(),
                slotId
        )) {
            throw new BadRequestException("A time slot already exists for the selected ground and time range");
        }

        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        return timeSlotRepository.save(timeSlot);
    }

    public void deleteTimeSlot(UUID slotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));
        if (Boolean.TRUE.equals(timeSlot.getIsBooked())) {
            throw new BadRequestException("Booked time slots cannot be deleted");
        }
        timeSlotRepository.delete(timeSlot);
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("End time must be after start time");
        }
    }
}
