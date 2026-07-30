package robertoCafagna.BE_capstone.DTO.RIDE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRouteRequestDTO(
        @NotBlank(message = "Inserire un nome per il percorso")
        String name,

        @Size(min = 2, message = "Servono almeno due punti (partenza e arrivo)")
        @Valid
        List<RouteWaypointRequestDTO> points,

        boolean avoidHighways,

        boolean avoidTolls,

        boolean avoidFerries
) {
}
