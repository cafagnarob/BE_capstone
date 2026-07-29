package robertoCafagna.BE_capstone.DTO.GARAGE;

public record UpdateVehicleRequestDTO(
        String nickname,
        Integer year,
        String licensePlate,
        String vin,
        String color
) {
}
