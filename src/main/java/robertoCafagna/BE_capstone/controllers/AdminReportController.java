package robertoCafagna.BE_capstone.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminReportSummaryDTO;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminResolveReportRequestDTO;
import robertoCafagna.BE_capstone.enums.ReportStatus;
import robertoCafagna.BE_capstone.enums.ReportTargetType;
import robertoCafagna.BE_capstone.services.ADMIN.AdminReportService;

import java.util.UUID;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    private final AdminReportService adminReportService;

    @GetMapping
    public ResponseEntity<Page<AdminReportSummaryDTO>> getAll(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportTargetType targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminReportService.getAll(status, targetType, page, size));
    }

    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<Void> dismiss(@PathVariable UUID id) {
        adminReportService.dismissReport(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(
            @PathVariable UUID id,
            @RequestBody(required = false) AdminResolveReportRequestDTO body
    ) {
        String reason = body != null ? body.reason() : null;
        adminReportService.resolveReport(id, reason);
        return ResponseEntity.noContent().build();
    }
}