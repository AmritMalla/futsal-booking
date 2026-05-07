package com.amrit.futsal.dto;

import com.amrit.futsal.entity.FutsalCompany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FutsalCompanyResponse {

    private UUID id;
    private UUID ownerId;
    private String ownerName;
    private String name;
    private String location;
    private LocalDateTime createdAt;

    public static FutsalCompanyResponse fromEntity(FutsalCompany company) {
        FutsalCompanyResponse response = new FutsalCompanyResponse();
        response.setId(company.getId());
        response.setOwnerId(company.getOwner().getId());
        response.setOwnerName(company.getOwner().getName());
        response.setName(company.getName());
        response.setLocation(company.getLocation());
        response.setCreatedAt(company.getCreatedAt());
        return response;
    }
}
