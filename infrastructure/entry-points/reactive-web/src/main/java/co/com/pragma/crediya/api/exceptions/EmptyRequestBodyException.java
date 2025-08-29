package co.com.pragma.crediya.api.exceptions;

import co.com.pragma.crediya.api.constants.WebErrorMessages;

public class EmptyRequestBodyException extends RuntimeException {

    public EmptyRequestBodyException() {
        super(WebErrorMessages.REQUEST_BODY_REQUIRED);
    }

}