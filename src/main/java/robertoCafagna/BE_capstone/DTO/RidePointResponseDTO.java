package robertoCafagna.BE_capstone.DTO;

public record RidePointResponseDTO(
        Double latitude,
        Double longitude,
        int sequence,
        Double speedKmh,
        Double altitude
) {
}
