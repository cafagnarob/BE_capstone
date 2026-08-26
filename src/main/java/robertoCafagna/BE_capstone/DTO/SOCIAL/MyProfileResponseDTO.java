package robertoCafagna.BE_capstone.DTO.SOCIAL;

import robertoCafagna.BE_capstone.DTO.GARAGE.VehicleSummaryDTO;
import robertoCafagna.BE_capstone.DTO.USER.ProfileLinkResponseDTO;
import robertoCafagna.BE_capstone.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MyProfileResponseDTO(
        UUID id,
        String username,
        String name,
        String surname,
        String email,
        String profilePicture,
        String description,
        String location,
        LocalDate birthDate,
        LocalDateTime createdAt,
        LocalDateTime lastLogin,
        boolean active,
        VehicleSummaryDTO currentVehicle,
        List<ProfileLinkResponseDTO> links,
        Role role
) {
}
