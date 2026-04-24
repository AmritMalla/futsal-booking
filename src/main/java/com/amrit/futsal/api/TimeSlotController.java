package com.amrit.futsal.api;

import com.amrit.futsal.dto.TimeSlotRequest;
import com.amrit.futsal.dto.TimeSlotResponse;
import com.amrit.futsal.service.AuthenticatedUserService;
import com.amrit.futsal.service.TimeSlotService;
import jakarta.validation.Valid;
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
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public TimeSlotController(TimeSlotService timeSlotService,
                              AuthenticatedUserService authenticatedUserService) {
        this.timeSlotService = timeSlotService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<TimeSlotResponse> createTimeSlot(@Valid @RequestBody TimeSlotRequest request) {
        authenticatedUserService.requireGroundOwnerOrAdmin(request.getGroundId());
        return ResponseEntity.ok(TimeSlotResponse.fromEntity(timeSlotService.createTimeSlot(request)));
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<TimeSlotResponse> getTimeSlotById(@PathVariable UUID slotId) {
        return timeSlotService.getTimeSlotById(slotId)
                .map(TimeSlotResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ground/{groundId}")
    public ResponseEntity<List<TimeSlotResponse>> getTimeSlotsByGroundId(@PathVariable UUID groundId) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByGroundId(groundId).stream()
                .map(TimeSlotResponse::fromEntity)
                .toList());
    }
    
    @GetMapping("/available/ground/{groundId}")
    public ResponseEntity<List<TimeSlotResponse>> getAvailableTimeSlots(@PathVariable UUID groundId) {
        return ResponseEntity.ok(timeSlotService.getAvailableTimeSlots(groundId).stream()
                .map(TimeSlotResponse::fromEntity)
                .toList());
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<TimeSlotResponse>> getTimeSlotsByDateRange(
            @RequestParam UUID groundId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByDateRange(groundId, start, end).stream()
                .map(TimeSlotResponse::fromEntity)
                .toList());
    }
    
    @PutMapping("/{slotId}")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(@PathVariable UUID slotId,
                                                           @Valid @RequestBody TimeSlotRequest request) {
        authenticatedUserService.requireTimeSlotOwnerOrAdmin(slotId);
        return ResponseEntity.ok(TimeSlotResponse.fromEntity(timeSlotService.updateTimeSlot(slotId, request)));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable UUID slotId) {
        authenticatedUserService.requireTimeSlotOwnerOrAdmin(slotId);
        timeSlotService.deleteTimeSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}
