package robertoCafagna.BE_capstone.DTO.ERROR;

import java.time.LocalDateTime;

public record ErrorDTO(String message, LocalDateTime timestamp) {
}
