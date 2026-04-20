package com.amrit.futsal.service;

import com.amrit.futsal.entity.TimeSlot;
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

    @Autowired
    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public TimeSlot createTimeSlot(TimeSlot timeSlot) {
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

    public TimeSlot updateTimeSlot(TimeSlot timeSlot) {
        return timeSlotRepository.save(timeSlot);
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
