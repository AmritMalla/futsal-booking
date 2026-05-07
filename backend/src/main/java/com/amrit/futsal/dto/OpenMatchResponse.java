package com.amrit.futsal.dto;

import com.amrit.futsal.entity.OpenMatch;
import com.amrit.futsal.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenMatchResponse {

    private UUID id;
    private UUID bookingId;
    private UUID groundId;
    private String groundName;
    private UUID slotId;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;
    private UUID hostUserId;
    private String hostName;
    private String title;
    private OpenMatch.SkillLevel skillLevel;
    private Integer desiredPlayerCount;
    private Integer currentPlayerCount;
    private Integer openSpots;
    private OpenMatch.OpenMatchStatus status;
    private String notes;
    private List<UUID> participantUserIds;
    private List<String> participantNames;
    private LocalDateTime createdAt;

    public static OpenMatchResponse fromEntity(OpenMatch openMatch) {
        List<User> participants = openMatch.getParticipants().stream().toList();
        int currentPlayerCount = 1 + participants.size();

        OpenMatchResponse response = new OpenMatchResponse();
        response.setId(openMatch.getId());
        response.setBookingId(openMatch.getBooking().getId());
        response.setGroundId(openMatch.getBooking().getGround().getId());
        response.setGroundName(openMatch.getBooking().getGround().getName());
        response.setSlotId(openMatch.getBooking().getSlot().getId());
        response.setSlotStartTime(openMatch.getBooking().getSlot().getStartTime());
        response.setSlotEndTime(openMatch.getBooking().getSlot().getEndTime());
        response.setHostUserId(openMatch.getHost().getId());
        response.setHostName(openMatch.getHost().getName());
        response.setTitle(openMatch.getTitle());
        response.setSkillLevel(openMatch.getSkillLevel());
        response.setDesiredPlayerCount(openMatch.getDesiredPlayerCount());
        response.setCurrentPlayerCount(currentPlayerCount);
        response.setOpenSpots(Math.max(0, openMatch.getDesiredPlayerCount() - currentPlayerCount));
        response.setStatus(openMatch.getStatus());
        response.setNotes(openMatch.getNotes());
        response.setParticipantUserIds(participants.stream().map(User::getId).toList());
        response.setParticipantNames(participants.stream().map(User::getName).toList());
        response.setCreatedAt(openMatch.getCreatedAt());
        return response;
    }
}
