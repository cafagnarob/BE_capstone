package robertoCafagna.BE_capstone.DTO.SOCIAL;

import jakarta.validation.constraints.NotNull;
import robertoCafagna.BE_capstone.enums.ReportReason;

public record CreateReportRequestDTO(
        @NotNull(message = "Specificare il motivo della segnalazione")
        ReportReason reason,
        String note
) {
}
