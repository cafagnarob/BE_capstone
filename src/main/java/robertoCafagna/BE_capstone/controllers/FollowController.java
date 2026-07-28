package robertoCafagna.BE_capstone.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.FollowStatsDTO;
import robertoCafagna.BE_capstone.DTO.FollowUserSummaryDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.FollowService;

@RestController
@RequestMapping("/users/{username}/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;


    @PostMapping
    public ResponseEntity<Void> follow(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String username
    ) {
        followService.follow(currentUser, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unfollow(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String username
    ) {
        followService.unfollow(currentUser, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers")
    public ResponseEntity<Page<FollowUserSummaryDTO>> getFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(followService.getFollowers(username, page, size));
    }

    @GetMapping("/following")
    public ResponseEntity<Page<FollowUserSummaryDTO>> getFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(followService.getFollowing(username, page, size));
    }

    @GetMapping("/stats")
    public ResponseEntity<FollowStatsDTO> getStats(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String username
    ) {
        return ResponseEntity.ok(followService.getStats(currentUser, username));
    }
}
