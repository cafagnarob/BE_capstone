package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.EVENT.*;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.EVENT.EventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDetailDTO> createEvent(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid CreateEventRequestDTO body
    ) {
        EventDetailDTO created = eventService.createEvent(currentUser, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<EventSummaryDTO>> getPublicEvents(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(eventService.getPublicEvents(currentUser));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EventSummaryDTO>> search(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        EventSearchFilterDTO filters = new EventSearchFilterDTO(title, dateFrom, dateTo, lat, lng, radiusKm);
        return ResponseEntity.ok(eventService.searchEvents(currentUser, filters, page, size));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailDTO> getEventById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(eventService.getEventById(currentUser, eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDetailDTO> updateEvent(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId,
            @RequestBody @Valid UpdateEventRequestDTO body
    ) {
        return ResponseEntity.ok(eventService.updateEvent(currentUser, eventId, body));
    }

    @PatchMapping("/{eventId}/status")
    public ResponseEntity<Void> changeStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId,
            @RequestBody @Valid ChangeEventStatusRequestDTO body
    ) {
        eventService.changeStatus(currentUser, eventId, body.status());
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{eventId}/access-code")
    public ResponseEntity<Void> regenerateAccessCode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId,
            @RequestBody @Valid RegenerateAccessCodeRequestDTO body
    ) {
        eventService.regenerateAccessCode(currentUser, eventId, body);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/access-code")
    public ResponseEntity<AccessCodeResponseDTO> getAccessCode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(eventService.getAccessCode(currentUser, eventId));
    }

    @GetMapping("/organized")
    public ResponseEntity<Page<EventSummaryDTO>> getOrganizedEvents(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(eventService.getOrganizedEvents(currentUser, page, size));
    }

    @GetMapping("/participating")
    public ResponseEntity<Page<EventSummaryDTO>> getParticipatingEvents(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(eventService.getParticipatingEvents(currentUser, page, size));
    }

    @PostMapping("/{tripId}/days")
    public ResponseEntity<EventDetailDTO> addDay(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID tripId,
            @RequestBody @Valid AddEventDayRequestDTO body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addDay(currentUser, tripId, body));
    }

    @PostMapping("/{eventId}/access-requests")
    public ResponseEntity<Void> requestAccessCode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId
    ) {
        eventService.requestAccessCode(currentUser, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{eventId}/access-requests")
    public ResponseEntity<List<AccessCodeRequestResponseDTO>> getAccessCodeRequests(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(eventService.getAccessCodeRequests(currentUser, eventId));
    }

    @PatchMapping("/{eventId}/access-requests/{requestId}/approve")
    public ResponseEntity<Void> approveAccessCodeRequest(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId,
            @PathVariable UUID requestId
    ) {
        eventService.approveAccessCodeRequest(currentUser, eventId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/access-requests/{requestId}/reject")
    public ResponseEntity<Void> rejectAccessCodeRequest(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID eventId,
            @PathVariable UUID requestId
    ) {
        eventService.rejectAccessCodeRequest(currentUser, eventId, requestId);
        return ResponseEntity.noContent().build();
    }
}
