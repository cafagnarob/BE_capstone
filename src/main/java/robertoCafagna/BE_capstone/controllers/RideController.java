package robertoCafagna.BE_capstone.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import robertoCafagna.BE_capstone.DTO.RIDE.FinishRideRequestDTO;
import robertoCafagna.BE_capstone.DTO.RIDE.RideDetailDTO;
import robertoCafagna.BE_capstone.DTO.RIDE.RideSummaryDTO;
import robertoCafagna.BE_capstone.DTO.RIDE.StartRideRequestDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.RIDE.RideService;

import java.util.UUID;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping
    public ResponseEntity<RideSummaryDTO> startRide(
            @AuthenticationPrincipal User currentUser,
            @RequestBody StartRideRequestDTO body
    ) {
        RideSummaryDTO ride = rideService.startRide(currentUser, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ride);
    }

    @PatchMapping("/{rideId}/finish")
    public ResponseEntity<RideDetailDTO> finishRide(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID rideId,
            @RequestBody @Valid FinishRideRequestDTO body
    ) {
        return ResponseEntity.ok(rideService.finishRide(currentUser, rideId, body));
    }

    @GetMapping
    public ResponseEntity<Page<RideSummaryDTO>> getMyRides(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(rideService.getUserRides(currentUser, vehicleId, page, size));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideDetailDTO> getRideById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID rideId
    ) {
        return ResponseEntity.ok(rideService.getRideById(currentUser, rideId));
    }

    @DeleteMapping("/{rideId}")
    public ResponseEntity<Void> deleteRide(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID rideId
    ) {
        rideService.deleteRide(currentUser, rideId);
        return ResponseEntity.noContent().build();
    }
}
