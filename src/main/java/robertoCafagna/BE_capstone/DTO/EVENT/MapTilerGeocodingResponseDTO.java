package robertoCafagna.BE_capstone.DTO.EVENT;

import java.util.List;

public record MapTilerGeocodingResponseDTO(
        List<MapTilerFeatureDTO> features
) {
}
