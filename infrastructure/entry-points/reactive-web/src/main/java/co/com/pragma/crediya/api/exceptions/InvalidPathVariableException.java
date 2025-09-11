package co.com.pragma.crediya.api.exceptions;

import co.com.pragma.crediya.api.constants.WebErrorMessages;

public class InvalidPathVariableException extends RuntimeException {

    public InvalidPathVariableException() {
        super(WebErrorMessages.INVALID_UUID);
    }

}