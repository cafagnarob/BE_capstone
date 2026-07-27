package robertoCafagna.BE_capstone.DTO;

import java.time.LocalDateTime;

public record ErrorDTO(String message, LocalDateTime timestamp) {
}
