package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.CreateInviteRequestDTO;
import robertoCafagna.BE_capstone.DTO.EventInviteResponseDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.EventInviteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EventInviteController {
    private final EventInviteService eventInviteService;

    @PostMapping("/events/{eventId}/invites")
    public ResponseEntity<EventInviteResponseDTO> invite(
            @AuthenticationPrincipal User organizer,
            @PathVariable UUID eventId,
            @RequestBody @Valid CreateInviteRequestDTO body
    ) {
        EventInviteResponseDTO invite = eventInviteService.invite(organizer, eventId, body.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(invite);
    }

    @GetMapping("/invites/me")
    public ResponseEntity<List<EventInviteResponseDTO>> getMyInvites(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(eventInviteService.getMyInvites(currentUser));
    }

    @PatchMapping("/invites/{inviteId}/accept")
    public ResponseEntity<EventInviteResponseDTO> accept(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID inviteId
    ) {
        return ResponseEntity.ok(eventInviteService.respond(currentUser, inviteId, true));
    }

    @PatchMapping("/invites/{inviteId}/reject")
    public ResponseEntity<EventInviteResponseDTO> reject(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID inviteId
    ) {
        return ResponseEntity.ok(eventInviteService.respond(currentUser, inviteId, false));
    }
}
