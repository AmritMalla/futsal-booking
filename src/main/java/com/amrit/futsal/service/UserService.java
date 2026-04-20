package com.amrit.futsal.service;

import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.DuplicateResourceException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User createUserWithDetails(String name, String email, String password, String phoneNumber, User.Role role) {
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("User with email " + email + " already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID userId, String name, String email, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (name != null) {
            user.setName(name);
        }

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new DuplicateResourceException("User with email " + email + " already exists");
            }
            user.setEmail(email);
        }

        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(UUID userId, User.Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setRole(role);
        return userRepository.save(user);
    }

    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }
}
