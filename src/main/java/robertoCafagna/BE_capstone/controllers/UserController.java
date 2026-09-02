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
import robertoCafagna.BE_capstone.DTO.ADMIN.ChangePasswordRequestDTO;
import robertoCafagna.BE_capstone.DTO.AUTH.UpdateUsernameRequestDTO;
import robertoCafagna.BE_capstone.DTO.ERROR.UpdateEmailRequestDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.MyProfileResponseDTO;
import robertoCafagna.BE_capstone.DTO.USER.*;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<MyProfileResponseDTO> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getMyProfile(currentUser));
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileResponseDTO> getPublicProfile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getPublicProfile(username));
    }

    @PutMapping("/me")
    public ResponseEntity<MyProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid UpdateProfileRequestDTO body
    ) {
        return ResponseEntity.ok(userService.updateProfile(currentUser, body));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid ChangePasswordRequestDTO body
    ) {
        userService.changePassword(currentUser, body);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/username")
    public ResponseEntity<MyProfileResponseDTO> updateUsername(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid UpdateUsernameRequestDTO body
    ) {
        return ResponseEntity.ok(userService.updateUsername(currentUser, body));
    }

    @PatchMapping("/me/email")
    public ResponseEntity<MyProfileResponseDTO> updateEmail(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid UpdateEmailRequestDTO body
    ) {
        return ResponseEntity.ok(userService.updateEmail(currentUser, body));
    }

    @PatchMapping(value = "/me/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateProfilePicture(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(userService.updateProfilePicture(currentUser, file));
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<Void> selectPresetAvatar(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid SelectAvatarRequestDTO body
    ) {
        userService.selectPresetAvatar(currentUser, body.avatarUrl());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/deactivate")
    public ResponseEntity<Void> deactivateAccount(@AuthenticationPrincipal User currentUser) {
        userService.deactivateAccount(currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/vehicle/{vehicleId}")
    public ResponseEntity<MyProfileResponseDTO> selectVehicle(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID vehicleId
    ) {
        return ResponseEntity.ok(userService.selectVehicle(currentUser, vehicleId));
    }

    @DeleteMapping("/me/vehicle")
    public ResponseEntity<MyProfileResponseDTO> clearVehicle(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.clearCurrentVehicle(currentUser));
    }

    @PostMapping("/me/links")
    public ResponseEntity<MyProfileResponseDTO> addProfileLink(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid ProfileLinkRequestDTO body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addProfileLink(currentUser, body));
    }

    @PutMapping("/me/links/{linkId}")
    public ResponseEntity<MyProfileResponseDTO> updateProfileLink(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID linkId,
            @RequestBody @Valid ProfileLinkRequestDTO body
    ) {
        return ResponseEntity.ok(userService.updateProfileLink(currentUser, linkId, body));
    }

    @DeleteMapping("/me/links/{linkId}")
    public ResponseEntity<MyProfileResponseDTO> deleteProfileLink(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID linkId
    ) {
        return ResponseEntity.ok(userService.deleteProfileLink(currentUser, linkId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserSearchResultDTO>> searchUsers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userService.searchUsers(currentUser, query, page, size));
    }
}
