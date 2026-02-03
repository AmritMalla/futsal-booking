package com.amrit.futsal.api;

import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.service.FutsalCompanyService;
import com.amrit.futsal.service.FutsalGroundService;
import com.amrit.futsal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private FutsalCompanyService futsalCompanyService;

    @Autowired
    private FutsalGroundService futsalGroundService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/owners")
    public ResponseEntity<List<User>> getAllOwners() {
        return ResponseEntity.ok(userService.getUsersByRole(User.Role.OWNER));
    }
    
    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        return ResponseEntity.ok(userService.getUsersByRole(User.Role.USER));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/companies")
    public ResponseEntity<List<FutsalCompany>> getAllCompanies() {
        return ResponseEntity.ok(futsalCompanyService.getAllFutsalCompanies());
    }

    @DeleteMapping("/companies/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID companyId) {
        futsalCompanyService.deleteFutsalCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grounds")
    public ResponseEntity<List<FutsalGround>> getAllGrounds() {
        return ResponseEntity.ok(futsalGroundService.getAllFutsalGrounds());
    }

    @DeleteMapping("/grounds/{groundId}")
    public ResponseEntity<Void> deleteGround(@PathVariable UUID groundId) {
        futsalGroundService.deleteFutsalGround(groundId);
        return ResponseEntity.noContent().build();
    }
}
