package co.com.pragma.crediya.model.loan.exceptions;

public class StatusNotFoundException extends RuntimeException {

    public StatusNotFoundException() {
        super(ErrorMessages.STATUS_NOT_FOUND);
    }

}