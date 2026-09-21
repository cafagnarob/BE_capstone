package robertoCafagna.BE_capstone.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminCancelEventRequestDTO;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminEventSearchFilterDTO;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminEventSummaryDTO;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventType;
import robertoCafagna.BE_capstone.enums.EventVisibility;
import robertoCafagna.BE_capstone.services.ADMIN.AdminEventService;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {
    private final AdminEventService adminEventService;


    @GetMapping
    public ResponseEntity<Page<AdminEventSummaryDTO>> getAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) EventVisibility visibility,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) String organizerUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean onlyPendingAccessRequests
    ) {
        AdminEventSearchFilterDTO filters = new AdminEventSearchFilterDTO(
                title, status, visibility, type, organizerUsername, dateFrom, dateTo, lat, lng, radiusKm,
                onlyPendingAccessRequests
        );
        return ResponseEntity.ok(adminEventService.getAll(filters, page, size));
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID eventId,
            @RequestBody(required = false) AdminCancelEventRequestDTO body
    ) {
        String reason = body != null ? body.reason() : null;
        adminEventService.cancelEvent(eventId, reason);
        return ResponseEntity.noContent().build();
    }
}