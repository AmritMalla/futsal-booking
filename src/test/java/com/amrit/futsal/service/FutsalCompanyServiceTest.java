package com.amrit.futsal.service;

import com.amrit.futsal.dto.FutsalCompanyRequest;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.DuplicateResourceException;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FutsalCompanyServiceTest {

    @Mock
    private FutsalCompanyRepository futsalCompanyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FutsalCompanyService futsalCompanyService;

    @Test
    void createFutsalCompanyUsesAuthenticatedOwner() {
        User owner = buildOwner();
        FutsalCompanyRequest request = new FutsalCompanyRequest(null, "Arena 7", "Lalitpur");

        when(futsalCompanyRepository.findByName(request.getName())).thenReturn(Optional.empty());
        when(futsalCompanyRepository.save(any(FutsalCompany.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FutsalCompany company = futsalCompanyService.createFutsalCompany(owner, request);

        assertEquals(owner, company.getOwner());
        assertEquals("Arena 7", company.getName());
        assertEquals("Lalitpur", company.getLocation());

        ArgumentCaptor<FutsalCompany> companyCaptor = ArgumentCaptor.forClass(FutsalCompany.class);
        verify(futsalCompanyRepository).save(companyCaptor.capture());
        assertEquals(owner, companyCaptor.getValue().getOwner());
    }

    @Test
    void createFutsalCompanyRejectsDuplicateName() {
        User owner = buildOwner();
        FutsalCompany existing = new FutsalCompany();
        existing.setId(UUID.randomUUID());
        existing.setName("Arena 7");
        FutsalCompanyRequest request = new FutsalCompanyRequest(null, "Arena 7", "Kathmandu");

        when(futsalCompanyRepository.findByName(request.getName())).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> futsalCompanyService.createFutsalCompany(owner, request));
    }

    private User buildOwner() {
        User owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setName("Owner");
        owner.setEmail(UUID.randomUUID() + "@example.com");
        owner.setRole(User.Role.OWNER);
        return owner;
    }
}
