package com.amrit.futsal.dto;

import com.amrit.futsal.entity.OpenMatch;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOpenMatchRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must not exceed 120 characters")
    private String title;

    @NotNull(message = "Skill level is required")
    private OpenMatch.SkillLevel skillLevel;

    @NotNull(message = "Desired player count is required")
    @Min(value = 2, message = "A match must have at least 2 total players")
    @Max(value = 14, message = "A match cannot exceed 14 total players")
    private Integer desiredPlayerCount;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
