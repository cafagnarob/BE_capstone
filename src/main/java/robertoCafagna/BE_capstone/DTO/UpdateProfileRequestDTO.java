package robertoCafagna.BE_capstone.DTO;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequestDTO(
        @Size(max = 50, message = "Il nome non può superare i 50 caratteri")
        String name,

        @Size(max = 50, message = "Il cognome non può superare i 50 caratteri")
        String surname,

        @Size(max = 500, message = "La descrizione non può superare i 500 caratteri")
        String description,

        @Size(max = 100, message = "La location non può superare i 100 caratteri")
        String location,

        LocalDate birthDate
) {
}
