package com.amrit.futsal.service;

import com.amrit.futsal.dto.OpenMatchRequest;
import com.amrit.futsal.dto.UpdateOpenMatchRequest;
import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.OpenMatch;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.OpenMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OpenMatchService {

    private final OpenMatchRepository openMatchRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public OpenMatchService(OpenMatchRepository openMatchRepository,
                            BookingRepository bookingRepository) {
        this.openMatchRepository = openMatchRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<OpenMatch> getOpenMatches() {
        return sortByStartTime(openMatchRepository.findDiscoverableMatches(LocalDateTime.now()));
    }

    public List<OpenMatch> getOpenMatchesByGroundId(UUID groundId) {
        return sortByStartTime(openMatchRepository.findDiscoverableMatchesByGroundId(groundId, LocalDateTime.now()));
    }

    public List<OpenMatch> getUserMatches(UUID userId) {
        return sortByStartTime(openMatchRepository.findUserRelatedMatches(userId));
    }

    public OpenMatch getOpenMatchById(UUID matchId) {
        return openMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("OpenMatch", "id", matchId));
    }

    @Transactional
    public OpenMatch createOpenMatch(User currentUser, OpenMatchRequest request) {
        Booking booking = getHostableBooking(request.getBookingId());

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only publish your own bookings as open matches");
        }
        if (openMatchRepository.existsByBookingId(booking.getId())) {
            throw new BadRequestException("This booking already has an open match");
        }

        OpenMatch openMatch = new OpenMatch();
        openMatch.setBooking(booking);
        openMatch.setHost(currentUser);
        openMatch.setTitle(request.getTitle().trim());
        openMatch.setSkillLevel(request.getSkillLevel());
        openMatch.setDesiredPlayerCount(request.getDesiredPlayerCount());
        openMatch.setNotes(trimToNull(request.getNotes()));
        openMatch.setStatus(calculateStatus(request.getDesiredPlayerCount(), 0));
        return openMatchRepository.save(openMatch);
    }

    @Transactional
    public OpenMatch updateOpenMatch(User currentUser, UUID matchId, UpdateOpenMatchRequest request) {
        OpenMatch openMatch = getManagedOpenMatch(currentUser, matchId);
        validateEditableOpenMatch(openMatch);

        int participantCount = openMatch.getParticipants().size();
        if (request.getDesiredPlayerCount() <= participantCount + 1) {
            throw new BadRequestException("Desired player count must stay above the current roster size");
        }

        openMatch.setTitle(request.getTitle().trim());
        openMatch.setSkillLevel(request.getSkillLevel());
        openMatch.setDesiredPlayerCount(request.getDesiredPlayerCount());
        openMatch.setNotes(trimToNull(request.getNotes()));
        openMatch.setStatus(calculateStatus(request.getDesiredPlayerCount(), participantCount));
        return openMatchRepository.save(openMatch);
    }

    @Transactional
    public OpenMatch joinOpenMatch(User currentUser, UUID matchId) {
        OpenMatch openMatch = getJoinableOpenMatch(matchId);

        if (openMatch.getHost().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Hosts are already part of the match");
        }
        boolean alreadyJoined = openMatch.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(currentUser.getId()));
        if (alreadyJoined) {
            throw new BadRequestException("You have already joined this match");
        }

        int participantCount = openMatch.getParticipants().size();
        int maxParticipants = openMatch.getDesiredPlayerCount() - 1;
        if (participantCount >= maxParticipants) {
            throw new BadRequestException("This match is already full");
        }

        openMatch.getParticipants().add(currentUser);
        openMatch.setStatus(calculateStatus(openMatch.getDesiredPlayerCount(), openMatch.getParticipants().size()));
        return openMatchRepository.save(openMatch);
    }

    @Transactional
    public OpenMatch leaveOpenMatch(User currentUser, UUID matchId) {
        OpenMatch openMatch = getOpenMatchById(matchId);

        if (openMatch.getHost().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Hosts cannot leave their own match. Cancel it instead.");
        }

        boolean removed = openMatch.getParticipants().removeIf(participant -> participant.getId().equals(currentUser.getId()));
        if (!removed) {
            throw new BadRequestException("You are not part of this match");
        }

        validateActiveBooking(openMatch.getBooking());
        openMatch.setStatus(calculateStatus(openMatch.getDesiredPlayerCount(), openMatch.getParticipants().size()));
        return openMatchRepository.save(openMatch);
    }

    @Transactional
    public void cancelOpenMatch(User currentUser, UUID matchId) {
        OpenMatch openMatch = getManagedOpenMatch(currentUser, matchId);
        openMatch.setStatus(OpenMatch.OpenMatchStatus.CANCELLED);
        openMatchRepository.save(openMatch);
    }

    private Booking getHostableBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        validateActiveBooking(booking);
        return booking;
    }

    private OpenMatch getJoinableOpenMatch(UUID matchId) {
        OpenMatch openMatch = getOpenMatchById(matchId);
        validateEditableOpenMatch(openMatch);
        return openMatch;
    }

    private OpenMatch getManagedOpenMatch(User currentUser, UUID matchId) {
        OpenMatch openMatch = getOpenMatchById(matchId);
        if (currentUser.getRole() != User.Role.ADMIN && !openMatch.getHost().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to manage this match");
        }
        return openMatch;
    }

    private void validateEditableOpenMatch(OpenMatch openMatch) {
        if (openMatch.getStatus() == OpenMatch.OpenMatchStatus.CANCELLED) {
            throw new BadRequestException("Cancelled matches cannot be modified");
        }
        validateActiveBooking(openMatch.getBooking());
    }

    private void validateActiveBooking(Booking booking) {
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BadRequestException("Open matches can only use confirmed bookings");
        }
        if (!booking.getSlot().getStartTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Only upcoming bookings can be used for open matches");
        }
    }

    private OpenMatch.OpenMatchStatus calculateStatus(int desiredPlayerCount, int participantCount) {
        return participantCount >= desiredPlayerCount - 1
                ? OpenMatch.OpenMatchStatus.FULL
                : OpenMatch.OpenMatchStatus.OPEN;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private List<OpenMatch> sortByStartTime(List<OpenMatch> matches) {
        return matches.stream()
                .sorted(Comparator.comparing(match -> match.getBooking().getSlot().getStartTime()))
                .toList();
    }
}
