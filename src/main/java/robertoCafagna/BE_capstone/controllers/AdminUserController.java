package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminDeactivateUserRequestDTO;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminDeleteUserRequestDTO;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminUserSummaryDTO;
import robertoCafagna.BE_capstone.services.ADMIN.AdminUserService;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;


    @GetMapping
    public ResponseEntity<Page<AdminUserSummaryDTO>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy
    ) {
        return ResponseEntity.ok(adminUserService.getAll(query, status, page, size, orderBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestBody @Valid AdminDeleteUserRequestDTO body
    ) {
        adminUserService.deleteUser(id, body.reason());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id,
            @RequestBody(required = false) AdminDeactivateUserRequestDTO body
    ) {
        String reason = body != null ? body.reason() : null;
        adminUserService.deactivateUser(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable UUID id) {
        adminUserService.reactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
