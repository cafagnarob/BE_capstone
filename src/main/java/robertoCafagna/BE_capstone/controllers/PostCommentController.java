package robertoCafagna.BE_capstone.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.CommentResponseDTO;
import robertoCafagna.BE_capstone.DTO.CreateCommentRequestDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.PostCommentService;

import java.util.UUID;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    @PostMapping
    public ResponseEntity<CommentResponseDTO> addComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @RequestBody @Valid CreateCommentRequestDTO body
    ) {
        CommentResponseDTO comment = postCommentService.addComment(currentUser, postId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponseDTO>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(postCommentService.getComments(postId, page, size));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @PathVariable UUID commentId
    ) {
        postCommentService.deleteComment(currentUser, postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
