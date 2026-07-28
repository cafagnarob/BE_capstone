package robertoCafagna.BE_capstone.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.JoinEventRequestDTO;
import robertoCafagna.BE_capstone.DTO.ParticipationResponseDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.EventParticipationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events/{eventId}/participations")
@RequiredArgsConstructor
public class EventParticipationController {
    private final EventParticipationService eventParticipationService;

    @PostMapping
    public ResponseEntity<ParticipationResponseDTO> join(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId,
            @RequestBody @Valid JoinEventRequestDTO body
    ) {
        ParticipationResponseDTO participation = eventParticipationService.join(currentUser, eventId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(participation);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> cancelMyParticipation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId
    ) {
        eventParticipationService.cancelMyParticipation(currentUser, eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ParticipationResponseDTO>> getPendingParticipants(
            @AuthenticationPrincipal User organizer,
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(eventParticipationService.getParticipants(organizer, eventId));
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<ParticipationResponseDTO>> getAcceptedParticipants(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(eventParticipationService.getAcceptedParticipants(currentUser, eventId));
    }

    @PatchMapping("/{participationId}/approve")
    public ResponseEntity<ParticipationResponseDTO> approve(
            @AuthenticationPrincipal User organizer,
            @PathVariable UUID eventId,
            @PathVariable UUID participationId
    ) {
        return ResponseEntity.ok(eventParticipationService.approve(organizer, eventId, participationId));
    }

    @PatchMapping("/{participationId}/reject")
    public ResponseEntity<ParticipationResponseDTO> reject(
            @AuthenticationPrincipal User organizer,
            @PathVariable UUID eventId,
            @PathVariable UUID participationId
    ) {
        return ResponseEntity.ok(eventParticipationService.reject(organizer, eventId, participationId));
    }
}