package co.com.pragma.crediya.model.loan.exceptions;

public class ErrorMessages {

    private ErrorMessages() {
    }

    public static final String TYPE_NOT_FOUND = "We couldn’t find the specified loan type.";

    public static final String STATUS_NOT_FOUND = "We couldn’t find the specified loan status.";

    public static final String REQUEST_BODY_REQUIRED = "Request body is required";

    public static final String INVALID_DECIMAL_FORMAT = "Invalid decimal format.";

    public static final String IDENTIFICATION_NUMBER_REQUIRED = "Identification number is required.";

    public static final String IDENTIFICATION_NUMBER_LENGTH = "Identification number must be exactly 10 digits.";

    public static final String INVALID_IDENTIFICATION_NUMBER_FORMAT = "Identification number must contain only digits.";

}
