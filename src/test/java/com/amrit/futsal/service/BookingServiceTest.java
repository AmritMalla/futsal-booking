package com.amrit.futsal.service;

import com.amrit.futsal.dto.BookingRequest;
import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.TimeSlot;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.TimeSlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private FutsalGroundRepository groundRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBookingUsesAuthenticatedUserInsteadOfRequestUser() {
        User authenticatedUser = buildUser();
        FutsalGround ground = buildGround();
        TimeSlot slot = buildSlot(ground);
        BookingRequest request = new BookingRequest(ground.getId(), slot.getId());

        when(groundRepository.findById(ground.getId())).thenReturn(Optional.of(ground));
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(authenticatedUser, request);

        assertEquals(authenticatedUser, booking.getUser());
        assertEquals(ground, booking.getGround());
        assertEquals(slot, booking.getSlot());
        assertEquals(Booking.BookingStatus.CONFIRMED, booking.getStatus());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertEquals(authenticatedUser, bookingCaptor.getValue().getUser());
    }

    @Test
    void createBookingRejectsSlotFromDifferentGround() {
        User authenticatedUser = buildUser();
        FutsalGround selectedGround = buildGround();
        FutsalGround differentGround = buildGround();
        TimeSlot slot = buildSlot(differentGround);
        BookingRequest request = new BookingRequest(selectedGround.getId(), slot.getId());

        when(groundRepository.findById(selectedGround.getId())).thenReturn(Optional.of(selectedGround));
        when(timeSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(authenticatedUser, request));
    }

    private User buildUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("user@example.com");
        user.setRole(User.Role.USER);
        return user;
    }

    private FutsalGround buildGround() {
        User owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(User.Role.OWNER);

        FutsalCompany company = new FutsalCompany();
        company.setId(UUID.randomUUID());
        company.setOwner(owner);

        FutsalGround ground = new FutsalGround();
        ground.setId(UUID.randomUUID());
        ground.setCompany(company);
        ground.setName("Test Ground");
        ground.setPricePerHour(BigDecimal.valueOf(1500));
        return ground;
    }

    private TimeSlot buildSlot(FutsalGround ground) {
        TimeSlot slot = new TimeSlot();
        slot.setId(UUID.randomUUID());
        slot.setGround(ground);
        slot.setStartTime(LocalDateTime.now().plusDays(1));
        slot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        slot.setIsBooked(false);
        return slot;
    }
}
