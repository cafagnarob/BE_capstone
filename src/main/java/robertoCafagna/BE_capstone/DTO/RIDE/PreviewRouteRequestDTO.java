package robertoCafagna.BE_capstone.DTO.RIDE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PreviewRouteRequestDTO(
        @Size(min = 2, message = "Servono almeno due punti")
        @Valid
        List<RouteWaypointRequestDTO> points,
        boolean avoidHighways,
        boolean avoidTolls,
        boolean avoidFerries
) {
}
