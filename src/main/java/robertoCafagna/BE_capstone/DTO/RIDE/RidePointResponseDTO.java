package robertoCafagna.BE_capstone.DTO.RIDE;

public record RidePointResponseDTO(
        Double latitude,
        Double longitude,
        int sequence,
        Double speedKmh,
        Double altitude
) {
}
