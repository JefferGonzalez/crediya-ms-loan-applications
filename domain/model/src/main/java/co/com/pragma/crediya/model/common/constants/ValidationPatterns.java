package co.com.pragma.crediya.model.common.constants;

public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    public static final String DECIMAL_REGEX = "^-?\\d+(\\.\\d{1,2})?$";

    public static final String IDENTIFICATION_NUMBER_REGEX = "^\\d+$";

    public static final String TERM_REGEX = "^(?:[1-9]|[1-9]\\d|1\\d\\d|2\\d\\d|3[0-5]\\d|360)$";

}
