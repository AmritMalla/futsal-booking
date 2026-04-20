package com.amrit.futsal.dto;

import com.amrit.futsal.entity.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotResponse {

    private UUID id;
    private UUID groundId;
    private String groundName;
    private String companyName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isBooked;
    private Long version;

    public static TimeSlotResponse fromEntity(TimeSlot timeSlot) {
        TimeSlotResponse response = new TimeSlotResponse();
        response.setId(timeSlot.getId());
        response.setGroundId(timeSlot.getGround().getId());
        response.setGroundName(timeSlot.getGround().getName());
        response.setCompanyName(timeSlot.getGround().getCompany().getName());
        response.setStartTime(timeSlot.getStartTime());
        response.setEndTime(timeSlot.getEndTime());
        response.setIsBooked(timeSlot.getIsBooked());
        response.setVersion(timeSlot.getVersion());
        return response;
    }
}
