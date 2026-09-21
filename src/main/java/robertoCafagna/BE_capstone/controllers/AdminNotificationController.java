package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import robertoCafagna.BE_capstone.DTO.ADMIN.BroadcastNotificationRequestDTO;
import robertoCafagna.BE_capstone.services.ADMIN.AdminNotificationService;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {
    private final AdminNotificationService adminNotificationService;

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcast(@RequestBody @Valid BroadcastNotificationRequestDTO body) {
        adminNotificationService.broadcastSystemMessage(body.message());
        return ResponseEntity.accepted().build();
    }
}
