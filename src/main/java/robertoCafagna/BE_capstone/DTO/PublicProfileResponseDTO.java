package robertoCafagna.BE_capstone.DTO;

import java.util.List;

public record PublicProfileResponseDTO(
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
