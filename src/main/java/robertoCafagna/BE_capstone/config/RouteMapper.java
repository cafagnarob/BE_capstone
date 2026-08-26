package robertoCafagna.BE_capstone.config;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import robertoCafagna.BE_capstone.DTO.RIDE.RouteResponseDTO;
import robertoCafagna.BE_capstone.DTO.RIDE.RouteWaypointResponseDTO;
import robertoCafagna.BE_capstone.entities.Route;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RouteMapper {

    public RouteResponseDTO toDTO(Route route) {
        List<RouteWaypointResponseDTO> waypointDTOs = route.getWaypoints().stream()
                .map(w -> new RouteWaypointResponseDTO(w.getLatitude(), w.getLongitude(), w.getSequence(), w.getLabel()))
                .toList();

        String googleMapsUrl = GoogleMapsLinkBuilder.buildNavigationUrl(route.getWaypoints());

        return new RouteResponseDTO(
                route.getId(), route.getName(), waypointDTOs,
                route.getEncodedPolyline(), route.getDistanceMeters(), route.getDurationSeconds(),
                route.isAvoidHighways(), route.isAvoidTolls(), route.isAvoidFerries(),
                googleMapsUrl, route.getCreatedAt(), route.isImportable(), false
        );
    }
}
