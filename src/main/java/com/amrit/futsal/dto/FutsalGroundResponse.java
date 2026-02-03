package com.amrit.futsal.dto;

import com.amrit.futsal.entity.FutsalGround;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FutsalGroundResponse {

    private UUID id;
    private UUID companyId;
    private String companyName;
    private String name;
    private String surfaceType;
    private BigDecimal pricePerHour;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static FutsalGroundResponse fromEntity(FutsalGround ground) {
        FutsalGroundResponse response = new FutsalGroundResponse();
        response.setId(ground.getId());
        response.setCompanyId(ground.getCompany().getId());
        response.setCompanyName(ground.getCompany().getName());
        response.setName(ground.getName());
        response.setSurfaceType(ground.getSurfaceType());
        response.setPricePerHour(ground.getPricePerHour());
        response.setImageUrl(ground.getImageUrl());
        response.setCreatedAt(ground.getCreatedAt());
        return response;
    }
}
