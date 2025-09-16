package co.com.pragma.crediya.model.loan.exceptions;

import co.com.pragma.crediya.model.common.validation.ValidationOutcome;
import co.com.pragma.crediya.model.loan.constants.ApplicationErrorMessages;

import java.util.List;

public class ApplicationBusinessValidationException extends RuntimeException {

    private final transient List<ValidationOutcome> errors;

    public ApplicationBusinessValidationException(List<ValidationOutcome> errors) {
        super(ApplicationErrorMessages.APPLICATION_BUSINESS_VALIDATION_FAILED);

        this.errors = errors;
    }

    public List<ValidationOutcome> getErrors() {
        return errors;
    }

}
