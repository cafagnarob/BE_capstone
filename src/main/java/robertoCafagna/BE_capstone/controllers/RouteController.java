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
import robertoCafagna.BE_capstone.DTO.RIDE.*;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.RIDE.RouteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RouteResponseDTO> createRoute(
            @AuthenticationPrincipal User currentUser,
            @RequestPart("data") @Valid CreateRouteRequestDTO body,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        RouteResponseDTO created = routeService.createRoute(currentUser, body, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping(value = "/{routeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RouteResponseDTO> updateRoute(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID routeId,
            @RequestPart("data") @Valid CreateRouteRequestDTO body,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity.ok(routeService.updateRoute(currentUser, routeId, body, images));
    }

    @GetMapping("/importable")
    public ResponseEntity<List<RouteResponseDTO>> getImportableRoutesForMap(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(routeService.getImportableRoutesForMap(currentUser));
    }


    @PatchMapping(value = "/{routeId}/waypoints/{waypointId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RouteWaypointResponseDTO> updateWaypointImage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID routeId,
            @PathVariable UUID waypointId,
            @RequestParam("image") MultipartFile image
    ) {
        return ResponseEntity.ok(routeService.updateWaypointImage(currentUser, routeId, waypointId, image));
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponseDTO> getRouteById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID routeId
    ) {
        return ResponseEntity.ok(routeService.getRouteById(currentUser, routeId));
    }


    @GetMapping("/my")
    public ResponseEntity<Page<RouteResponseDTO>> getMyRoutes(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(routeService.getMyRoutes(currentUser, page, size));
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<Void> deleteRoute(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID routeId
    ) {
        routeService.deleteRoute(currentUser, routeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{routeId}/importable")
    public ResponseEntity<Void> setImportable(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID routeId,
            @RequestParam boolean value
    ) {
        routeService.setImportable(currentUser, routeId, value);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{routeId}/import")
    public ResponseEntity<RouteResponseDTO> importRoute(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID routeId
    ) {
        RouteResponseDTO imported = routeService.importRoute(currentUser, routeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }

    @PostMapping("/preview")
    public ResponseEntity<RoutePreviewDTO> preview(@RequestBody @Valid PreviewRouteRequestDTO body) {
        return ResponseEntity.ok(routeService.previewRoute(body));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Page<RouteResponseDTO>> getUserRoutes(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(routeService.getUserRoutes(currentUser, username, page, size));
    }
}
