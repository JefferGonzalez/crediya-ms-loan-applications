package co.com.pragma.crediya.model.loan.constants;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public final class DomainConstants {

    private DomainConstants() {
    }

    public static final String DEFAULT_PENDING_STATUS = "UNDER REVIEW";

    public static final int IDENTIFICATION_NUMBER_LENGTH = 10;

    public static final String INVALID_IDENTIFICATION_NUMBER_FORMAT = "Identification number must contain only digits.";

    public static final String AMOUNT_FIELD = "Amount";

    public static final String MOST_BE_BETWEEN = "most be between";

    public static final String AND_CONNECTOR = "and";

    public static final int MAX_LENGTH_TERM = 360;

    public static final int MIN_LENGTH_TERM = 1;

    public static final int MAX_LENGTH_EMAIL = 100;

    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.00");

}