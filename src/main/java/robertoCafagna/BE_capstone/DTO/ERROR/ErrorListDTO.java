package robertoCafagna.BE_capstone.DTO.ERROR;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorListDTO(String message,
                           LocalDateTime timestamp, List<String> errorList) {
}
