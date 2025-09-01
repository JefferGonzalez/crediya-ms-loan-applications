package co.com.pragma.crediya.model.loan.exceptions;

import co.com.pragma.crediya.model.loan.constants.ApplicationErrorMessages;

public class StatusNotFoundException extends RuntimeException {

    public StatusNotFoundException() {
        super(ApplicationErrorMessages.STATUS_NOT_FOUND);
    }

}