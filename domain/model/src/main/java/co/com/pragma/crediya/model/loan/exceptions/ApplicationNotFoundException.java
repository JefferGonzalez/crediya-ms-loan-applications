package co.com.pragma.crediya.model.loan.exceptions;

import co.com.pragma.crediya.model.loan.constants.ApplicationErrorMessages;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException() {
        super(ApplicationErrorMessages.APPLICATION_NOT_FOUND);
    }

}
