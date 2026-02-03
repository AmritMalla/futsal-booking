package com.amrit.futsal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Ground ID is required")
    private UUID groundId;

    @NotNull(message = "Time slot ID is required")
    private UUID slotId;
}
