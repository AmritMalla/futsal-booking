package com.amrit.futsal.api;

import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.service.FutsalCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
public class FutsalCompanyController {

    private final FutsalCompanyService futsalCompanyService;

    @Autowired
    public FutsalCompanyController(FutsalCompanyService futsalCompanyService) {
        this.futsalCompanyService = futsalCompanyService;
    }

    @PostMapping
    public ResponseEntity<FutsalCompany> createFutsalCompany(@RequestBody FutsalCompany futsalCompany) {
        return ResponseEntity.ok(futsalCompanyService.createFutsalCompany(futsalCompany));
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<FutsalCompany> getFutsalCompanyById(@PathVariable UUID companyId) {
        return futsalCompanyService.getFutsalCompanyById(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<FutsalCompany> getFutsalCompanyByName(@PathVariable String name) {
        return futsalCompanyService.getFutsalCompanyByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<FutsalCompany>> getFutsalCompaniesByOwnerId(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(futsalCompanyService.getFutsalCompaniesByOwnerId(ownerId));
    }

    @GetMapping
    public ResponseEntity<List<FutsalCompany>> getAllFutsalCompanies() {
        return ResponseEntity.ok(futsalCompanyService.getAllFutsalCompanies());
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteFutsalCompany(@PathVariable UUID companyId) {
        futsalCompanyService.deleteFutsalCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
