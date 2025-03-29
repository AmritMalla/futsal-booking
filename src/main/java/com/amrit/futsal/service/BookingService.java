package com.amrit.futsal.service;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Optional<Booking> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<Booking> getBookingsByCustomerId(Long customerId) {
        return bookingRepository.findByCustomer_UserId(customerId);
    }

    public void deleteBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }
}
