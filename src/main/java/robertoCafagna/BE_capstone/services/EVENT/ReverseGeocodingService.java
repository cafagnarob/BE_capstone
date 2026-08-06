package robertoCafagna.BE_capstone.services.EVENT;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import robertoCafagna.BE_capstone.DTO.EVENT.MapTilerGeocodingResponseDTO;

import java.util.Locale;

@Service
@Slf4j
public class ReverseGeocodingService {
    private final RestClient restClient = RestClient.create();

    @Value("${maptiler.access-key}")
    private String accessKey;

    public String reverseGeocode(double latitude, double longitude) {
        String url = String.format(
                Locale.US,
                "https://api.maptiler.com/geocoding/%f,%f.json?key=%s&language=it&limit=1",
                longitude, latitude, accessKey
        );

        try {
            MapTilerGeocodingResponseDTO response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(MapTilerGeocodingResponseDTO.class);

            if (response == null || response.features() == null || response.features().isEmpty()) {
                return null;
            }
            return response.features().get(0).placeName();
        } catch (Exception e) {
            log.warn("Reverse geocoding fallito per {},{}", latitude, longitude, e);
            return null;
        }
    }
}
