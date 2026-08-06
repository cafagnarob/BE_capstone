package robertoCafagna.BE_capstone.DTO.USER;

import robertoCafagna.BE_capstone.DTO.GARAGE.VehicleSummaryDTO;

import java.util.List;
import java.util.UUID;

public record PublicProfileResponseDTO(
        UUID id,
        String username,
        String name,
        String surname,
        String profilePicture,
        String description,
        String location,
        VehicleSummaryDTO currentVehicle,
        List<ProfileLinkResponseDTO> links
) {
}
