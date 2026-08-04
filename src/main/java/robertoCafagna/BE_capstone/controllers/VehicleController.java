package robertoCafagna.BE_capstone.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.GARAGE.CreateVehicleRequestDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.UpdateVehicleRequestDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.VehicleResponseDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.services.GARAGE.VehicleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> getMyVehicles(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(vehicleService.getUserVehicles(currentUser));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehicleResponseDTO> addVehicle(
            @AuthenticationPrincipal User currentUser,
            @RequestPart("data") @Valid CreateVehicleRequestDTO body,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        VehicleResponseDTO created = vehicleService.addVehicle(currentUser, body, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID vehicleId,
            @RequestBody @Valid UpdateVehicleRequestDTO body
    ) {
        return ResponseEntity.ok(vehicleService.updateVehicle(currentUser, vehicleId, body));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID vehicleId
    ) {
        vehicleService.deleteVehicle(currentUser, vehicleId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping(value = "/{vehicleId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehicleResponseDTO> updatePhoto(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID vehicleId,
            @RequestPart("photo") MultipartFile photo
    ) {
        return ResponseEntity.ok(vehicleService.updateVehiclePhoto(currentUser, vehicleId, photo));
    }

    @DeleteMapping("/{vehicleId}/photo")
    public ResponseEntity<VehicleResponseDTO> deletePhoto(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID vehicleId
    ) {
        return ResponseEntity.ok(vehicleService.deleteVehiclePhoto(currentUser, vehicleId));
    }
}
