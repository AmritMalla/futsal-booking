package com.amrit.futsal.api;

import com.amrit.futsal.dto.PaymentRequest;
import com.amrit.futsal.dto.PaymentResponse;
import com.amrit.futsal.entity.Payment;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.service.AuthenticatedUserService;
import com.amrit.futsal.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public PaymentController(PaymentService paymentService,
                             AuthenticatedUserService authenticatedUserService) {
        this.paymentService = paymentService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.processPayment(authenticatedUserService.getCurrentUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.fromEntity(payment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        authenticatedUserService.requirePaymentAccess(paymentId);
        Payment payment = paymentService.getPaymentById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentByTransactionId(@PathVariable String transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));
        authenticatedUserService.requirePaymentAccess(payment.getId());
        return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBookingId(@PathVariable UUID bookingId) {
        authenticatedUserService.requireBookingAccess(bookingId);
        List<PaymentResponse> payments = paymentService.getPaymentsByBookingId(bookingId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable UUID userId) {
        authenticatedUserService.requireCurrentUserOrAdmin(userId);
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable Payment.PaymentStatus status) {
        authenticatedUserService.requireAdmin();
        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable UUID paymentId,
            @RequestParam Payment.PaymentStatus status) {
        authenticatedUserService.requirePaymentManagementAccess(paymentId);
        Payment payment = paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable UUID paymentId) {
        authenticatedUserService.requirePaymentAccess(paymentId);
        Payment payment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(PaymentResponse.fromEntity(payment));
    }
}
