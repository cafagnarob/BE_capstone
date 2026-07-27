package robertoCafagna.BE_capstone.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private List<String> errorsList;

    public ValidationException(List<String> errorsList) {
        super("Errori di validazione");
        this.errorsList = errorsList;
    }

    public ValidationException(String message) {
        super(message);
    }
}

