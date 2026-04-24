package com.amrit.futsal.service;

import com.amrit.futsal.dto.TimeSlotRequest;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.dto.TimeSlotBulkRequest;
import com.amrit.futsal.repository.TimeSlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final FutsalGroundRepository futsalGroundRepository;

    @Autowired
    public TimeSlotService(TimeSlotRepository timeSlotRepository, FutsalGroundRepository futsalGroundRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.futsalGroundRepository = futsalGroundRepository;
    }

    @Transactional
    public TimeSlot createTimeSlot(TimeSlotRequest request) {
        FutsalGround ground = futsalGroundRepository.findById(request.getGroundId())
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "id", request.getGroundId()));

        // Check for overlaps
        if (timeSlotRepository.countOverlappingSlots(request.getGroundId(), request.getStartTime(), request.getEndTime()) > 0) {
            throw new IllegalArgumentException("Time slot overlaps with an existing slot for this ground");
        }

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setGround(ground);
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setIsBooked(false);

        log.info("Creating time slot for ground: {} from {} to {}", request.getGroundId(), request.getStartTime(), request.getEndTime());
        return timeSlotRepository.save(timeSlot);
    }

    @Transactional
    public List<TimeSlot> createBulkTimeSlots(TimeSlotBulkRequest request) {
        FutsalGround ground = futsalGroundRepository.findById(request.getGroundId())
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "id", request.getGroundId()));

        List<TimeSlot> createdSlots = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();

        log.info("Generating bulk time slots for ground: {} from {} to {}", request.getGroundId(), request.getStartDate(), request.getEndDate());

        while (!currentDate.isAfter(request.getEndDate())) {
            LocalDateTime currentDateTime = LocalDateTime.of(currentDate, request.getOpenTime());

            LocalDateTime closingDateTime;
            if (request.getCloseTime().isBefore(request.getOpenTime()) || request.getCloseTime().equals(request.getOpenTime())) {
                // If close time is before or equal to open time, assume it closes the next day (e.g. late night)
                closingDateTime = LocalDateTime.of(currentDate.plusDays(1), request.getCloseTime());
            } else {
                closingDateTime = LocalDateTime.of(currentDate, request.getCloseTime());
            }

            while (!currentDateTime.plusMinutes(request.getSlotDurationMinutes()).isAfter(closingDateTime)) {

                LocalDateTime startTime = currentDateTime;
                LocalDateTime endTime = startTime.plusMinutes(request.getSlotDurationMinutes());

                // Ensure no overlapping slots
                if (timeSlotRepository.countOverlappingSlots(ground.getId(), startTime, endTime) == 0) {
                    TimeSlot timeSlot = new TimeSlot();
                    timeSlot.setGround(ground);
                    timeSlot.setStartTime(startTime);
                    timeSlot.setEndTime(endTime);
                    timeSlot.setIsBooked(false);

                    createdSlots.add(timeSlot);
                } else {
                    log.warn("Skipping slot from {} to {} due to overlap", startTime, endTime);
                }

                currentDateTime = currentDateTime.plusMinutes(request.getSlotDurationMinutes());
            }
            currentDate = currentDate.plusDays(1);
        }

        log.info("Successfully generated {} time slots", createdSlots.size());
        return timeSlotRepository.saveAll(createdSlots);
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
        TimeSlot existingSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));

        FutsalGround ground = futsalGroundRepository.findById(request.getGroundId())
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "id", request.getGroundId()));

        existingSlot.setGround(ground);
        existingSlot.setStartTime(request.getStartTime());
        existingSlot.setEndTime(request.getEndTime());

        log.info("Updating time slot ID: {}", slotId);
        return timeSlotRepository.save(existingSlot);
    }
    
    public void markSlotAsBooked(UUID slotId) {
        Optional<TimeSlot> timeSlotOpt = timeSlotRepository.findById(slotId);
        if (timeSlotOpt.isPresent()) {
            TimeSlot timeSlot = timeSlotOpt.get();
            timeSlot.setIsBooked(true);
            timeSlotRepository.save(timeSlot);
        }
    }

    public void deleteTimeSlot(UUID slotId) {
        timeSlotRepository.deleteById(slotId);
    }
}
