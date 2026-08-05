package robertoCafagna.BE_capstone.DTO.RIDE;

public record RoutePreviewDTO(
        String encodedPolyline,
        double distanceMeters,
        double durationSeconds
) {
}
