package com.amrit.futsal.api;

import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.service.FutsalGroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grounds")
public class FutsalGroundController {

    private final FutsalGroundService futsalGroundService;

    @Autowired
    public FutsalGroundController(FutsalGroundService futsalGroundService) {
        this.futsalGroundService = futsalGroundService;
    }

    @PostMapping
    public ResponseEntity<FutsalGround> createFutsalGround(@RequestBody FutsalGround futsalGround) {
        return ResponseEntity.ok(futsalGroundService.createFutsalGround(futsalGround));
    }

    @GetMapping("/{groundId}")
    public ResponseEntity<FutsalGround> getFutsalGroundById(@PathVariable UUID groundId) {
        return futsalGroundService.getFutsalGroundById(groundId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<FutsalGround> getFutsalGroundByName(@PathVariable String name) {
        return futsalGroundService.getFutsalGroundByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<FutsalGround>> getFutsalGroundsByCompanyId(@PathVariable UUID companyId) {
        return ResponseEntity.ok(futsalGroundService.getFutsalGroundsByCompanyId(companyId));
    }

    @GetMapping("/surface/{surfaceType}")
    public ResponseEntity<List<FutsalGround>> getFutsalGroundsBySurfaceType(@PathVariable String surfaceType) {
        return ResponseEntity.ok(futsalGroundService.getFutsalGroundsBySurfaceType(surfaceType));
    }

    @GetMapping
    public ResponseEntity<List<FutsalGround>> getAllFutsalGrounds() {
        return ResponseEntity.ok(futsalGroundService.getAllFutsalGrounds());
    }

    @DeleteMapping("/{groundId}")
    public ResponseEntity<Void> deleteFutsalGround(@PathVariable UUID groundId) {
        futsalGroundService.deleteFutsalGround(groundId);
        return ResponseEntity.noContent().build();
    }
}
