package com.amrit.futsal.service;

import com.amrit.futsal.dto.FutsalGroundRequest;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.exception.BadRequestException;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FutsalGroundServiceTest {

    @Mock
    private FutsalGroundRepository futsalGroundRepository;

    @Mock
    private FutsalCompanyRepository futsalCompanyRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FutsalGroundService futsalGroundService;

    @Test
    void updateFutsalGroundRejectsChangingCompany() {
        UUID currentCompanyId = UUID.randomUUID();
        FutsalGround ground = new FutsalGround();
        ground.setId(UUID.randomUUID());
        ground.setCompany(buildCompany(currentCompanyId));
        ground.setName("Arena A");

        FutsalGroundRequest request = new FutsalGroundRequest(
                UUID.randomUUID(),
                "Arena A",
                "Indoor",
                BigDecimal.valueOf(1800),
                null
        );

        when(futsalGroundRepository.findById(ground.getId())).thenReturn(Optional.of(ground));

        assertThrows(BadRequestException.class, () -> futsalGroundService.updateFutsalGround(ground.getId(), request));
    }

    private FutsalCompany buildCompany(UUID companyId) {
        FutsalCompany company = new FutsalCompany();
        company.setId(companyId);
        return company;
    }
}
