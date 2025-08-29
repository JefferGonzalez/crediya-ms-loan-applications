package co.com.pragma.crediya.model.loan.exceptions;

import co.com.pragma.crediya.model.loan.constants.ApplicationErrorMessages;

public class TypeNotFoundException extends RuntimeException {

    public TypeNotFoundException() {
        super(ApplicationErrorMessages.TYPE_NOT_FOUND);
    }

}