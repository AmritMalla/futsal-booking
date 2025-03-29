package com.amrit.futsal.service;

import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public Optional<TimeSlot> getTimeSlotById(Long slotId) {
        return timeSlotRepository.findById(slotId);
    }

    public List<TimeSlot> getTimeSlotsByGroundId(Long groundId) {
        return timeSlotRepository.findByGround_GroundId(groundId);
    }

    public List<TimeSlot> getAvailableTimeSlots() {
        return timeSlotRepository.findByStatus(TimeSlot.Status.AVAILABLE);
    }

    public List<TimeSlot> getTimeSlotsWithinRange(LocalDateTime start, LocalDateTime end) {
        return timeSlotRepository.findByStartTimeBetween(start, end);
    }

    public void deleteTimeSlot(Long slotId) {
        timeSlotRepository.deleteById(slotId);
    }

    public List<TimeSlot> findTimeSlotsByGroundAndDate(Long groundId, LocalDateTime date){
        // Calculate the start of the day
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();

        // Calculate the end of the day
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59, 999999999);

        // Call the other method with calculated start and end
        return timeSlotRepository.findTimeSlotsByGroundAndStartTimeBetween(groundId, startOfDay, endOfDay);
    }
}
