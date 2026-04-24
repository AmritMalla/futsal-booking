package com.amrit.futsal.api;

import com.amrit.futsal.dto.FutsalCompanyRequest;
import com.amrit.futsal.dto.FutsalCompanyResponse;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.service.AuthenticatedUserService;
import com.amrit.futsal.service.FutsalCompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
public class FutsalCompanyController {

    private final FutsalCompanyService futsalCompanyService;
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public FutsalCompanyController(FutsalCompanyService futsalCompanyService,
                                   AuthenticatedUserService authenticatedUserService) {
        this.futsalCompanyService = futsalCompanyService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<FutsalCompanyResponse> createFutsalCompany(@Valid @RequestBody FutsalCompanyRequest request) {
        authenticatedUserService.requireOwnerOrAdmin();
        User currentUser = authenticatedUserService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FutsalCompanyResponse.fromEntity(futsalCompanyService.createFutsalCompany(currentUser, request)));
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<FutsalCompanyResponse> getFutsalCompanyById(@PathVariable UUID companyId) {
        return futsalCompanyService.getFutsalCompanyById(companyId)
                .map(FutsalCompanyResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<FutsalCompanyResponse> getFutsalCompanyByName(@PathVariable String name) {
        return futsalCompanyService.getFutsalCompanyByName(name)
                .map(FutsalCompanyResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<FutsalCompanyResponse>> getFutsalCompaniesByOwnerId(@PathVariable UUID ownerId) {
        authenticatedUserService.requireCurrentUserOrAdmin(ownerId);
        return ResponseEntity.ok(futsalCompanyService.getFutsalCompaniesByOwnerId(ownerId)
                .stream()
                .map(FutsalCompanyResponse::fromEntity)
                .toList());
    }

    @GetMapping("/me")
    public ResponseEntity<List<FutsalCompanyResponse>> getMyFutsalCompanies() {
        User currentUser = authenticatedUserService.getCurrentUser();
        return ResponseEntity.ok(futsalCompanyService.getFutsalCompaniesByOwnerId(currentUser.getId())
                .stream()
                .map(FutsalCompanyResponse::fromEntity)
                .toList());
    }

    @GetMapping
    public ResponseEntity<Page<FutsalCompanyResponse>> getAllFutsalCompanies(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(futsalCompanyService.getAllFutsalCompanies(pageable)
                .map(FutsalCompanyResponse::fromEntity));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<FutsalCompanyResponse> updateFutsalCompany(@PathVariable UUID companyId,
                                                                     @Valid @RequestBody FutsalCompanyRequest request) {
        authenticatedUserService.requireCompanyOwnerOrAdmin(companyId);
        return ResponseEntity.ok(FutsalCompanyResponse.fromEntity(
                futsalCompanyService.updateOwnedFutsalCompany(companyId, request)
        ));
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteFutsalCompany(@PathVariable UUID companyId) {
        authenticatedUserService.requireCompanyOwnerOrAdmin(companyId);
        futsalCompanyService.deleteFutsalCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
