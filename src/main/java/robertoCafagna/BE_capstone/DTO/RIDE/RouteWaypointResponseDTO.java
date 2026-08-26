package robertoCafagna.BE_capstone.DTO.RIDE;

public record RouteWaypointResponseDTO(
        double latitude, double longitude, int sequence, String label
) {
}
