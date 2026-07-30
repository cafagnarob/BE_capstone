package robertoCafagna.BE_capstone.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.RIDE.CreateRouteRequestDTO;
import robertoCafagna.BE_capstone.DTO.RIDE.RouteResponseDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.RIDE.RouteService;

import java.util.UUID;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<RouteResponseDTO> createRoute(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid CreateRouteRequestDTO body
    ) {
        RouteResponseDTO created = routeService.createRoute(currentUser, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponseDTO> getRouteById(@PathVariable UUID routeId) {
        return ResponseEntity.ok(routeService.getRouteById(routeId));
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
}
