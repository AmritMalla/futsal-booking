package com.amrit.futsal.service;

import com.amrit.futsal.dto.BookingRequest;
import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.exception.SlotNotAvailableException;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.TimeSlotRepository;
import com.amrit.futsal.repository.UserRepository;
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
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final FutsalGroundRepository groundRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          TimeSlotRepository timeSlotRepository,
                          UserRepository userRepository,
                          FutsalGroundRepository groundRepository) {
        this.bookingRepository = bookingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.groundRepository = groundRepository;
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        // Fetch user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Fetch ground
        FutsalGround ground = groundRepository.findById(request.getGroundId())
                .orElseThrow(() -> new ResourceNotFoundException("Ground", "id", request.getGroundId()));

        // Fetch time slot
        TimeSlot slot = timeSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", request.getSlotId()));

        // Check if slot is already booked
        if (slot.getIsBooked()) {
            throw new SlotNotAvailableException("The selected time slot is already booked");
        }

        // Check if slot belongs to the selected ground
        if (!slot.getGround().getId().equals(ground.getId())) {
            throw new BadRequestException("The selected time slot does not belong to the specified ground");
        }

        // Mark the time slot as booked
        slot.setIsBooked(true);
        timeSlotRepository.save(slot);

        // Create and save booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setGround(ground);
        booking.setSlot(slot);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

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

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking updateBooking(UUID bookingId, BookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Fetch updated references if IDs have changed
        if (request.getUserId() != null && !booking.getUser().getId().equals(request.getUserId())) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
            booking.setUser(user);
        }

        if (request.getGroundId() != null && !booking.getGround().getId().equals(request.getGroundId())) {
            FutsalGround ground = groundRepository.findById(request.getGroundId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ground", "id", request.getGroundId()));
            booking.setGround(ground);
        }

        if (request.getSlotId() != null && !booking.getSlot().getId().equals(request.getSlotId())) {
            TimeSlot newSlot = timeSlotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", request.getSlotId()));

            if (newSlot.getIsBooked()) {
                throw new SlotNotAvailableException("The selected time slot is already booked");
            }

            // Free up old slot
            TimeSlot oldSlot = booking.getSlot();
            oldSlot.setIsBooked(false);
            timeSlotRepository.save(oldSlot);

            // Mark new slot as booked
            newSlot.setIsBooked(true);
            timeSlotRepository.save(newSlot);

            booking.setSlot(newSlot);
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBookingStatus(UUID bookingId, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        booking.setStatus(status);

        // If booking is cancelled, free up the time slot
        if (status == Booking.BookingStatus.CANCELLED) {
            TimeSlot slot = booking.getSlot();
            slot.setIsBooked(false);
            timeSlotRepository.save(slot);
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed booking");
        }

        // Free up the time slot
        TimeSlot slot = booking.getSlot();
        slot.setIsBooked(false);
        timeSlotRepository.save(slot);

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public void deleteBooking(UUID bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Booking", "id", bookingId);
        }
        bookingRepository.deleteById(bookingId);
    }
}
