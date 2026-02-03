package com.amrit.futsal.api;

import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @Autowired
    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping
    public ResponseEntity<TimeSlot> createTimeSlot(@RequestBody TimeSlot timeSlot) {
        return ResponseEntity.ok(timeSlotService.createTimeSlot(timeSlot));
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<TimeSlot> getTimeSlotById(@PathVariable UUID slotId) {
        return timeSlotService.getTimeSlotById(slotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ground/{groundId}")
    public ResponseEntity<List<TimeSlot>> getTimeSlotsByGroundId(@PathVariable UUID groundId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByGroundId(groundId));
    }
    
    @GetMapping("/available/ground/{groundId}")
    public ResponseEntity<List<TimeSlot>> getAvailableTimeSlots(@PathVariable UUID groundId) {
        return ResponseEntity.ok(timeSlotService.getAvailableTimeSlots(groundId));
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<TimeSlot>> getTimeSlotsByDateRange(
            @RequestParam UUID groundId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByDateRange(groundId, start, end));
    }
    
    @PutMapping("/{slotId}/book")
    public ResponseEntity<TimeSlot> markSlotAsBooked(@PathVariable UUID slotId) {
        timeSlotService.markSlotAsBooked(slotId);
        return timeSlotService.getTimeSlotById(slotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{slotId}")
    public ResponseEntity<TimeSlot> updateTimeSlot(@PathVariable UUID slotId, @RequestBody TimeSlot timeSlot) {
        if (!slotId.equals(timeSlot.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(timeSlotService.updateTimeSlot(timeSlot));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable UUID slotId) {
        timeSlotService.deleteTimeSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}
