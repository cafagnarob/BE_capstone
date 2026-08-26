package robertoCafagna.BE_capstone.DTO.RIDE;

import robertoCafagna.BE_capstone.enums.RideType;

import java.util.UUID;

public record StartRideRequestDTO(
        UUID vehicleId,
        String title,
        RideType type
) {
}
