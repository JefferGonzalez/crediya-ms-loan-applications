package co.com.pragma.crediya.model.loan.exceptions;

import co.com.pragma.crediya.model.loan.constants.ApplicationConstants;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;

import java.math.BigDecimal;

public class ApplicationValueOutOfBoundsException extends RuntimeException {

    public ApplicationValueOutOfBoundsException(BigDecimal minValue, BigDecimal maxValue) {
        super(buildMessage(minValue, maxValue));
    }

    private static String buildMessage(BigDecimal minValue, BigDecimal maxValue) {
        String minValueFormatted = ApplicationConstants.DECIMAL_FORMAT.format(minValue);
        String maxValueFormatted = ApplicationConstants.DECIMAL_FORMAT.format(maxValue);

        return String.join(" ",
                ApplicationFieldNames.AMOUNT,
                ApplicationConstants.MOST_BE_BETWEEN,
                minValueFormatted,
                ApplicationConstants.AND_CONNECTOR,
                maxValueFormatted
        );
    }

}

