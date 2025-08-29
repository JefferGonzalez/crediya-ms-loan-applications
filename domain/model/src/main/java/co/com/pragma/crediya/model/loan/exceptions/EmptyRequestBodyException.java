package co.com.pragma.crediya.model.loan.exceptions;

public class EmptyRequestBodyException extends RuntimeException {

    public EmptyRequestBodyException() {
        super(ErrorMessages.REQUEST_BODY_REQUIRED);
    }

}