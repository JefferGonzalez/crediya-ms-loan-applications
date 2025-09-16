package co.com.pragma.crediya.model.loan.exceptions;

import co.com.pragma.crediya.model.loan.constants.ApplicationConstants;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;

public class ApplicationTermOutOfBoundsException extends RuntimeException {

    public ApplicationTermOutOfBoundsException(int minValue, int maxValue) {
        super(buildMessage(minValue, maxValue));
    }

    private static String buildMessage(int minValue, int maxValue) {
        return String.join(" ",
                ApplicationFieldNames.TERM,
                ApplicationConstants.MOST_BE_BETWEEN,
                String.valueOf(minValue),
                ApplicationConstants.AND_CONNECTOR,
                String.valueOf(maxValue)
        );
    }

}
