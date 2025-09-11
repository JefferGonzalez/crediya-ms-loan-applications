package co.com.pragma.crediya.model.loan.constants;

import java.text.DecimalFormat;

public final class ApplicationConstants {

    private ApplicationConstants() {
    }

    public static final String MOST_BE_BETWEEN = "most be between";

    public static final String AND_CONNECTOR = "and";

    public static final String OR_CONNECTOR = "or";

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.00");

    public static final String APPLICATION_INVALID_STATUS =
            "Application with id '%s' cannot be processed. Current status '%s' is not allowed. Permitted statuses: %s.";

    public static final String APPLICATION_CANNOT_BE_PROCESSED =
            "Application with id '%s' cannot be processed. Only applications under %s can be approved or rejected.";

    public static final String LOAN_STATUS_UPDATE_SUBJECT = "Loan Application Status Update";

    public static final String LOAN_STATUS_UPDATE_BODY_TEMPLATE = "Your loan application has been %s.";

}