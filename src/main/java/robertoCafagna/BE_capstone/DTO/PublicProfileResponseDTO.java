package robertoCafagna.BE_capstone.DTO;

public record PublicProfileResponseDTO(
        String username,
        String name,
        String surname,
        String profilePicture,
        String description,
        String location,
        VehicleSummaryDTO currentVehicle
) {
}
