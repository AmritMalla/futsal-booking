package com.amrit.futsal.api;

import com.amrit.futsal.dto.BookingRequest;
import com.amrit.futsal.dto.BookingResponse;
import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.fromEntity(booking));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID bookingId) {
        Booking booking = bookingService.getBookingById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable UUID userId) {
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId)
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/ground/{groundId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByGroundId(@PathVariable UUID groundId) {
        List<BookingResponse> bookings = bookingService.getBookingsByGroundId(groundId)
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable Booking.BookingStatus status) {
        List<BookingResponse> bookings = bookingService.getBookingsByStatus(status)
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<BookingResponse>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<BookingResponse> bookings = bookingService.getBookingsByDateRange(startDate, endDate)
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable UUID bookingId,
            @RequestParam Booking.BookingStatus status) {
        Booking updatedBooking = bookingService.updateBookingStatus(bookingId, status);
        return ResponseEntity.ok(BookingResponse.fromEntity(updatedBooking));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID bookingId) {
        Booking cancelledBooking = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(BookingResponse.fromEntity(cancelledBooking));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
