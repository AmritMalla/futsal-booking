package com.amrit.futsal.api;

import com.amrit.futsal.config.JwtTokenUtil;
import com.amrit.futsal.dto.UserResponse;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.DuplicateResourceException;
import com.amrit.futsal.model.LoginRequest;
import com.amrit.futsal.model.LoginResponse;
import com.amrit.futsal.model.RegisterRequest;
import com.amrit.futsal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtTokenUtil.generateToken(userDetails);
        
        User user = userService.getUserByEmail(userDetails.getUsername()).orElseThrow();
        
        return ResponseEntity.ok(new LoginResponse(jwt, user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if email already exists
        if (userService.getUserByEmail(registerRequest.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", registerRequest.getEmail());
        }

        // Create new user's account
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(registerRequest.getRole());

        User savedUser = userService.createUser(user);
        
        // Authenticate the new user and generate token
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(savedUser.getEmail())
                .password(savedUser.getPasswordHash())
                .roles(savedUser.getRole().name())
                .build();
        
        String jwt = jwtTokenUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(new LoginResponse(jwt, savedUser.getEmail(), savedUser.getRole().name()));
    }
}
