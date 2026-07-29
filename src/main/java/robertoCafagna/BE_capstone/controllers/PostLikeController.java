package robertoCafagna.BE_capstone.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.SOCIAL.LikeStatusDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.SOCIAL.PostLikeService;

import java.util.UUID;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    public ResponseEntity<LikeStatusDTO> like(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(postLikeService.like(currentUser, postId));
    }

    @DeleteMapping
    public ResponseEntity<LikeStatusDTO> unlike(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(postLikeService.unlike(currentUser, postId));
    }

    @GetMapping
    public ResponseEntity<LikeStatusDTO> getStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(postLikeService.getStatus(currentUser, postId));
    }
}
