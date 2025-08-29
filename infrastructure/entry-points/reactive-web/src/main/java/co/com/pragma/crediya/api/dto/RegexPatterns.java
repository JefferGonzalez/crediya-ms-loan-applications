package co.com.pragma.crediya.api.dto;


public final class RegexPatterns {
    private RegexPatterns() {
    }

    public static final String NAME_REGEX = "^[\\p{L}]+(?:[\\s'\\-][\\p{L}]+)*$";

    public static final String DECIMAL_REGEX = "^-?\\d+(\\.\\d{1,2})?$";

    public static final String IDENTIFICATION_NUMBER_REGEX = "^\\d+$";

}
