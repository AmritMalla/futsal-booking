package com.amrit.futsal.api;

import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.service.FutsalGroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<FutsalGround>> getGroundsByFutsalId(@RequestParam Long futsalId) {
        return ResponseEntity.ok(futsalGroundService.getFutsalGroundsByFutsalId(futsalId));
    }

    @GetMapping("/{groundId}")
    public ResponseEntity<FutsalGround> getGroundById(@PathVariable Long groundId) {
        return futsalGroundService.getFutsalGroundById(groundId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
