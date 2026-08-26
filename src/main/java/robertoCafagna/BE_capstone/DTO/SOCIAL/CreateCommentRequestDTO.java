package robertoCafagna.BE_capstone.DTO.SOCIAL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequestDTO(
        @NotBlank(message = "Il commento non può essere vuoto")
        @Size(max = 500, message = "Il commento non può superare i 500 caratteri")
        String text
) {
}
