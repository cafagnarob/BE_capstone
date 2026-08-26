package robertoCafagna.BE_capstone.DTO.GARAGE;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;

import java.util.UUID;

public record CreateMotorcycleModelRequestDTO(
        @NotNull(message = "Specificare il brand")
        UUID brandId,

        @NotBlank(message = "Inserire il nome del modello")
        String name,

        @Min(value = 1, message = "La cilindrata deve essere positiva")
        int engineCc,

        @NotNull(message = "Specificare la categoria")
        MotorcycleCategory category,

        @Min(value = 1900, message = "Anno non valido")
        int yearStart,

        Integer yearEnd,

        @NotNull(message = "Inserire la potenza")
        @Min(value = 1, message = "La potenza deve essere positiva")
        Integer horsePower,

        Integer weightKg
) {
}
