package robertoCafagna.BE_capstone.DTO.EVENT;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MapTilerFeatureDTO(
        @JsonProperty("place_name") String placeName
) {
}
