package com.amrit.futsal.api;

import com.amrit.futsal.dto.UserResponse;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.service.AuthenticatedUserService;
import com.amrit.futsal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public UserController(UserService userService,
                          AuthenticatedUserService authenticatedUserService) {
        this.userService = userService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody User user) {
        authenticatedUserService.requireAdmin();
        return ResponseEntity.ok(UserResponse.fromEntity(userService.createUser(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(UserResponse.fromEntity(authenticatedUserService.getCurrentUser()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        authenticatedUserService.requireCurrentUserOrAdmin(userId);
        return userService.getUserById(userId)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        authenticatedUserService.requireAdmin();
        return userService.getUserByEmail(email)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        authenticatedUserService.requireAdmin();
        return ResponseEntity.ok(userService.getAllUsers(pageable).map(UserResponse::fromEntity));
    }

    @GetMapping("/by-role")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@RequestParam("role") User.Role role) {
        authenticatedUserService.requireAdmin();
        return ResponseEntity.ok(userService.getUsersByRole(role).stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        authenticatedUserService.requireAdmin();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
