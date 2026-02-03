package com.amrit.futsal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FutsalGroundRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Ground name is required")
    @Size(min = 2, max = 100, message = "Ground name must be between 2 and 100 characters")
    private String name;

    @Size(max = 50, message = "Surface type must not exceed 50 characters")
    private String surfaceType;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal pricePerHour;

    private String imageUrl;
}
