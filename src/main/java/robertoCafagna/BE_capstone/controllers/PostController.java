package robertoCafagna.BE_capstone.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.SOCIAL.CreatePostRequestDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.PostResponseDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.FeedType;
import robertoCafagna.BE_capstone.services.SOCIAL.PostService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> createPost(
            @AuthenticationPrincipal User currentUser,
            @RequestPart("data") @Valid CreatePostRequestDTO body,
            @RequestPart(value = "media", required = false) List<MultipartFile> media
    ) {
        PostResponseDTO created = postService.createPost(currentUser, body, media);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> getFeed(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "FOLLOWING") FeedType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(postService.getFeed(currentUser, type, page, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponseDTO>> getUserPosts(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(postService.getUserPosts(currentUser, userId, page, size));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPostById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(postService.getPostById(currentUser, postId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID postId
    ) {
        postService.deletePost(currentUser, postId);
        return ResponseEntity.noContent().build();
    }
}
