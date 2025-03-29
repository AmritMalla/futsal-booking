package com.amrit.futsal.api;

import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.service.FutsalCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<FutsalCompany>> getAllFutsalCompanies() {
        return ResponseEntity.ok(futsalCompanyService.getAllFutsalCompanies());
    }

    @GetMapping("/{futsalId}")
    public ResponseEntity<FutsalCompany> getFutsalCompanyById(@PathVariable Long futsalId) {
        return futsalCompanyService.getFutsalCompanyById(futsalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
