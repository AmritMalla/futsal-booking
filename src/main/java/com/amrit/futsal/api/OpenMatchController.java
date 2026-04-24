package com.amrit.futsal.api;

import com.amrit.futsal.dto.OpenMatchRequest;
import com.amrit.futsal.dto.OpenMatchResponse;
import com.amrit.futsal.dto.UpdateOpenMatchRequest;
import com.amrit.futsal.entity.OpenMatch;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.service.AuthenticatedUserService;
import com.amrit.futsal.service.OpenMatchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/open-matches")
public class OpenMatchController {

    private final OpenMatchService openMatchService;
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public OpenMatchController(OpenMatchService openMatchService,
                               AuthenticatedUserService authenticatedUserService) {
        this.openMatchService = openMatchService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping
    public ResponseEntity<List<OpenMatchResponse>> getOpenMatches() {
        return ResponseEntity.ok(openMatchService.getOpenMatches().stream()
                .map(OpenMatchResponse::fromEntity)
                .toList());
    }

    @GetMapping("/ground/{groundId}")
    public ResponseEntity<List<OpenMatchResponse>> getOpenMatchesByGroundId(@PathVariable UUID groundId) {
        return ResponseEntity.ok(openMatchService.getOpenMatchesByGroundId(groundId).stream()
                .map(OpenMatchResponse::fromEntity)
                .toList());
    }

    @GetMapping("/me")
    public ResponseEntity<List<OpenMatchResponse>> getMyMatches() {
        User currentUser = authenticatedUserService.getCurrentUser();
        return ResponseEntity.ok(openMatchService.getUserMatches(currentUser.getId()).stream()
                .map(OpenMatchResponse::fromEntity)
                .toList());
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<OpenMatchResponse> getOpenMatchById(@PathVariable UUID matchId) {
        OpenMatch openMatch = openMatchService.getOpenMatchById(matchId);
        return ResponseEntity.ok(OpenMatchResponse.fromEntity(openMatch));
    }

    @PostMapping
    public ResponseEntity<OpenMatchResponse> createOpenMatch(@Valid @RequestBody OpenMatchRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();
        OpenMatch openMatch = openMatchService.createOpenMatch(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OpenMatchResponse.fromEntity(openMatch));
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<OpenMatchResponse> updateOpenMatch(@PathVariable UUID matchId,
                                                             @Valid @RequestBody UpdateOpenMatchRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();
        OpenMatch openMatch = openMatchService.updateOpenMatch(currentUser, matchId, request);
        return ResponseEntity.ok(OpenMatchResponse.fromEntity(openMatch));
    }

    @PostMapping("/{matchId}/join")
    public ResponseEntity<OpenMatchResponse> joinOpenMatch(@PathVariable UUID matchId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        OpenMatch openMatch = openMatchService.joinOpenMatch(currentUser, matchId);
        return ResponseEntity.ok(OpenMatchResponse.fromEntity(openMatch));
    }

    @PostMapping("/{matchId}/leave")
    public ResponseEntity<OpenMatchResponse> leaveOpenMatch(@PathVariable UUID matchId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        OpenMatch openMatch = openMatchService.leaveOpenMatch(currentUser, matchId);
        return ResponseEntity.ok(OpenMatchResponse.fromEntity(openMatch));
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> cancelOpenMatch(@PathVariable UUID matchId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        openMatchService.cancelOpenMatch(currentUser, matchId);
        return ResponseEntity.noContent().build();
    }
}
