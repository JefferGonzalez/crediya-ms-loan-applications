package co.com.pragma.crediya.model.loan.exceptions;

public class TypeNotFoundException extends RuntimeException {

    public TypeNotFoundException() {
        super(ErrorMessages.TYPE_NOT_FOUND);
    }

}