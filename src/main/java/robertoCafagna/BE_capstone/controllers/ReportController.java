package robertoCafagna.BE_capstone.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import robertoCafagna.BE_capstone.DTO.SOCIAL.CreateReportRequestDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.SOCIAL.ReportService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<Void> reportPost(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @RequestBody @Valid CreateReportRequestDTO body
    ) {
        reportService.reportPost(currentUser, postId, body);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/report")
    public ResponseEntity<Void> reportComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @RequestBody @Valid CreateReportRequestDTO body
    ) {
        reportService.reportComment(currentUser, commentId, body);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/users/{username}/report")
    public ResponseEntity<Void> reportUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String username,
            @RequestBody @Valid CreateReportRequestDTO body
    ) {
        reportService.reportUser(currentUser, username, body);
        return ResponseEntity.status(201).build();
    }
}