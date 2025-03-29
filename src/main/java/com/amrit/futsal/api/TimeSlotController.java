package com.amrit.futsal.api;

import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<TimeSlot>> getSlotsByGroundId(@RequestParam Long groundId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByGroundId(groundId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<TimeSlot>> getAvailableSlots() {
        return ResponseEntity.ok(timeSlotService.getAvailableTimeSlots());
    }




}
