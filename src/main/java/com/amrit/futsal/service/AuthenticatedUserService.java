package com.amrit.futsal.service;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.repository.BookingRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.repository.PaymentRepository;
import com.amrit.futsal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final FutsalGroundRepository groundRepository;

    @Autowired
    public AuthenticatedUserService(UserRepository userRepository,
                                    BookingRepository bookingRepository,
                                    PaymentRepository paymentRepository,
                                    FutsalGroundRepository groundRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.groundRepository = groundRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required to access this resource");
        }

        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String principalString && !"anonymousUser".equals(principalString)) {
            email = principalString;
        } else {
            throw new AccessDeniedException("Authentication is required to access this resource");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public void requireAdmin() {
        if (getCurrentUser().getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can access this resource");
        }
    }

    public void requireCurrentUserOrAdmin(UUID userId) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to access this user");
        }
    }

    public void requireGroundOwnerOrAdmin(UUID groundId) {
        User currentUser = getCurrentUser();
        FutsalGround ground = groundRepository.findById(groundId)
                .orElseThrow(() -> new ResourceNotFoundException("Ground", "id", groundId));

        if (!isAdmin(currentUser) && !ground.getCompany().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this ground");
        }
    }

    public void requireBookingAccess(UUID bookingId) {
        User currentUser = getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!isAdmin(currentUser)
                && !booking.getUser().getId().equals(currentUser.getId())
                && !booking.getGround().getCompany().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this booking");
        }
    }

    public void requireBookingOwnerOrAdmin(UUID bookingId) {
        User currentUser = getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!isAdmin(currentUser) && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to modify this booking");
        }
    }

    public void requireBookingManagementAccess(UUID bookingId) {
        User currentUser = getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!isAdmin(currentUser)
                && !booking.getGround().getCompany().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to manage this booking");
        }
    }

    public void requirePaymentAccess(UUID paymentId) {
        User currentUser = getCurrentUser();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (!isAdmin(currentUser)
                && !payment.getUser().getId().equals(currentUser.getId())
                && !payment.getBooking().getGround().getCompany().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this payment");
        }
    }

    public void requirePaymentManagementAccess(UUID paymentId) {
        User currentUser = getCurrentUser();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (!isAdmin(currentUser)
                && !payment.getBooking().getGround().getCompany().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to manage this payment");
        }
    }

    private boolean isAdmin(User user) {
        return user.getRole() == User.Role.ADMIN;
    }
}
