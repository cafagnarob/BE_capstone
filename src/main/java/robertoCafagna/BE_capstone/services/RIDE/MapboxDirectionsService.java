package robertoCafagna.BE_capstone.services.RIDE;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import robertoCafagna.BE_capstone.DTO.RIDE.MapboxDirectionsResponse;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapboxDirectionsService {

    private final MapboxRateLimiter rateLimiter;
    private final RestClient restClient = RestClient.create();

    @Value("${mapbox.access-token}")
    private String accessToken;

    public DirectionsResult calculateRoute(List<double[]> points, boolean avoidHighways, boolean avoidTolls, boolean avoidFerries) {
        if (points.size() < 2) {
            throw new BadRequestException("Servono almeno due punti per calcolare un percorso");
        }
        rateLimiter.checkAndIncrement();

        String coordinates = points.stream()
                .map(p -> p[1] + "," + p[0]) // Mapbox vuole lng,lat
                .collect(Collectors.joining(";"));

        List<String> excludeList = new ArrayList<>();
        if (avoidHighways) excludeList.add("motorway");
        if (avoidTolls) excludeList.add("toll");
        if (avoidFerries) excludeList.add("ferry");


        StringBuilder url = new StringBuilder("https://api.mapbox.com/directions/v5/mapbox/driving/")
                .append(coordinates)
                .append("?geometries=polyline&overview=full");

        if (!excludeList.isEmpty()) {
            url.append("&exclude=").append(String.join(",", excludeList));
        }

        url.append("&access_token=").append(URLEncoder.encode(accessToken, StandardCharsets.UTF_8));

        MapboxDirectionsResponse response;
        try {
            response = restClient.get()
                    .uri(url.toString())
                    .retrieve()
                    .body(MapboxDirectionsResponse.class);
        } catch (Exception e) {
            log.error("Errore durante la chiamata a Mapbox Directions", e);
            throw new BadRequestException("Impossibile calcolare il percorso in questo momento");
        }

        if (response == null || response.routes() == null || response.routes().isEmpty()) {
            throw new BadRequestException("Nessun percorso stradale trovato tra i punti indicati");
        }

        var route = response.routes().get(0);
        return new DirectionsResult(route.geometry(), route.distance(), route.duration());
    }

    public record DirectionsResult(String encodedPolyline, double distanceMeters, double durationSeconds) {
    }
}
