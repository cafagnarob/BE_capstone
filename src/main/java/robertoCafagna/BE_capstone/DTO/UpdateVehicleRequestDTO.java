package robertoCafagna.BE_capstone.DTO;

public record UpdateVehicleRequestDTO(
        String nickname,
        Integer year,
        String licensePlate,
        String vin,
        String color
) {
}
