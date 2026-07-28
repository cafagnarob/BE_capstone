package robertoCafagna.BE_capstone.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleResponseDTO(
        UUID id,
        MotorcycleModelResponseDTO model,
        String nickname,
        int year,
        String licensePlate,
        String vin,
        String color,
        int initialMileage,
        int currentMileage,
        String photoUrl,
        LocalDateTime createdAt
) {
}
