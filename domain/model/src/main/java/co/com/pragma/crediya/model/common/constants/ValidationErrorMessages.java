package co.com.pragma.crediya.model.common.constants;

public final class ValidationErrorMessages {

    private ValidationErrorMessages() {
    }

    public static final String AMOUNT_REQUIRED = "Amount is required.";

    public static final String TERM_REQUIRED = "Term is required.";

    public static final String INVALID_DECIMAL_FORMAT = "Invalid decimal format.";

    public static final String IDENTIFICATION_NUMBER_REQUIRED = "Identification number is required.";

    public static final String IDENTIFICATION_NUMBER_LENGTH = "Identification number must be exactly 10 digits.";

    public static final String INVALID_IDENTIFICATION_NUMBER_FORMAT = "Identification number must contain only digits.";

    public static final String TYPE_REQUIRED = "Type is required.";

    public static final String TERM_RANGE = "Term must be a number between 1 and 360";

}
