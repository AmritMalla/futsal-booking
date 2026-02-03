package com.amrit.futsal.service;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TimeSlotService timeSlotService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, TimeSlotService timeSlotService) {
        this.bookingRepository = bookingRepository;
        this.timeSlotService = timeSlotService;
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        // Mark the time slot as booked
        timeSlotService.markSlotAsBooked(booking.getSlot().getId());
        // Save the booking
        return bookingRepository.save(booking);
    }

    public Optional<Booking> getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<Booking> getBookingsByUserId(UUID userId) {
        return bookingRepository.findByUserId(userId);
    }
    
    public List<Booking> getBookingsByGroundId(UUID groundId) {
        return bookingRepository.findByGroundId(groundId);
    }
    
    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }
    
    public List<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findByBookingDateBetween(startDate, endDate);
    }
    
    public Booking updateBookingStatus(UUID bookingId, Booking.BookingStatus status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(status);
            return bookingRepository.save(booking);
        }
        return null;
    }

    public void deleteBooking(UUID bookingId) {
        bookingRepository.deleteById(bookingId);
    }
}
