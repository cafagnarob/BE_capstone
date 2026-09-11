package robertoCafagna.BE_capstone.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.SOCIAL.CommentResponseDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.CreateCommentRequestDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.SOCIAL.PostCommentService;

import java.util.List;
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
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(postCommentService.getComments(currentUser, postId, page, size));
    }

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentResponseDTO> addReply(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @RequestBody @Valid CreateCommentRequestDTO body
    ) {
        CommentResponseDTO reply = postCommentService.addReply(currentUser, postId, commentId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponseDTO>> getReplies(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @PathVariable UUID commentId
    ) {
        return ResponseEntity.ok(postCommentService.getReplies(currentUser, commentId));
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> toggleCommentLike(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId,
            @PathVariable UUID commentId
    ) {
        postCommentService.toggleCommentLike(currentUser, postId, commentId);
        return ResponseEntity.noContent().build();
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
