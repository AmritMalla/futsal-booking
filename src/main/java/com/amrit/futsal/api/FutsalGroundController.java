package com.amrit.futsal.api;

import com.amrit.futsal.dto.FutsalGroundRequest;
import com.amrit.futsal.dto.FutsalGroundResponse;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.service.FutsalGroundService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/grounds")
public class FutsalGroundController {

    private final FutsalGroundService futsalGroundService;

    @Autowired
    public FutsalGroundController(FutsalGroundService futsalGroundService) {
        this.futsalGroundService = futsalGroundService;
    }

    @PostMapping
    public ResponseEntity<FutsalGroundResponse> createFutsalGround(
            @Valid @RequestBody FutsalGroundRequest request) {
        FutsalGround ground = futsalGroundService.createFutsalGround(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(FutsalGroundResponse.fromEntity(ground));
    }

    @GetMapping("/{groundId}")
    public ResponseEntity<FutsalGroundResponse> getFutsalGroundById(@PathVariable UUID groundId) {
        FutsalGround ground = futsalGroundService.getFutsalGroundById(groundId)
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "id", groundId));
        return ResponseEntity.ok(FutsalGroundResponse.fromEntity(ground));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<FutsalGroundResponse> getFutsalGroundByName(@PathVariable String name) {
        FutsalGround ground = futsalGroundService.getFutsalGroundByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "name", name));
        return ResponseEntity.ok(FutsalGroundResponse.fromEntity(ground));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<FutsalGroundResponse>> getFutsalGroundsByCompanyId(
            @PathVariable UUID companyId) {
        List<FutsalGroundResponse> grounds = futsalGroundService.getFutsalGroundsByCompanyId(companyId)
                .stream()
                .map(FutsalGroundResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(grounds);
    }

    @GetMapping("/surface/{surfaceType}")
    public ResponseEntity<List<FutsalGroundResponse>> getFutsalGroundsBySurfaceType(
            @PathVariable String surfaceType) {
        List<FutsalGroundResponse> grounds = futsalGroundService.getFutsalGroundsBySurfaceType(surfaceType)
                .stream()
                .map(FutsalGroundResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(grounds);
    }

    @GetMapping
    public ResponseEntity<Page<FutsalGroundResponse>> getAllFutsalGrounds(@PageableDefault(size = 20) Pageable pageable) {
        Page<FutsalGroundResponse> grounds = futsalGroundService.getAllFutsalGrounds(pageable)
                .map(FutsalGroundResponse::fromEntity);
        return ResponseEntity.ok(grounds);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FutsalGroundResponse>> searchGrounds(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String surfaceType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<FutsalGroundResponse> grounds = futsalGroundService
                .searchGrounds(location, surfaceType, minPrice, maxPrice)
                .stream()
                .map(FutsalGroundResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(grounds);
    }

    @PutMapping("/{groundId}")
    public ResponseEntity<FutsalGroundResponse> updateFutsalGround(
            @PathVariable UUID groundId,
            @Valid @RequestBody FutsalGroundRequest request) {
        FutsalGround ground = futsalGroundService.updateFutsalGround(groundId, request);
        return ResponseEntity.ok(FutsalGroundResponse.fromEntity(ground));
    }

    @PostMapping(value = "/{groundId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FutsalGroundResponse> uploadGroundImage(
            @PathVariable UUID groundId,
            @RequestParam("file") MultipartFile file) {
        FutsalGround ground = futsalGroundService.updateGroundImage(groundId, file);
        return ResponseEntity.ok(FutsalGroundResponse.fromEntity(ground));
    }

    @DeleteMapping("/{groundId}")
    public ResponseEntity<Void> deleteFutsalGround(@PathVariable UUID groundId) {
        futsalGroundService.deleteFutsalGround(groundId);
        return ResponseEntity.noContent().build();
    }
}
