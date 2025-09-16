package co.com.pragma.crediya.usecase.loan.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ApplicationCalculatorUtils {

    private static final int CALCULATION_SCALE = 10;

    private static final int RESULT_SCALE = 2;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final BigDecimal MONTHS_IN_YEAR_PERCENT = BigDecimal.valueOf(1200);

    private ApplicationCalculatorUtils() {
    }

    public static BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal interestRate, int term) {
        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            return amount.divide(BigDecimal.valueOf(term), RESULT_SCALE, ROUNDING_MODE);
        }

        // R = (amount * interestRate) / (1 - (1 + interestRate)^(-term))
        BigDecimal numerator = amount.multiply(interestRate);
        BigDecimal onePlusRate = BigDecimal.ONE.add(interestRate);
        BigDecimal powerResult = onePlusRate.pow(term);
        BigDecimal denominator = BigDecimal.ONE.subtract(
                BigDecimal.ONE.divide(powerResult, CALCULATION_SCALE, ROUNDING_MODE)
        );

        return numerator.divide(denominator, RESULT_SCALE, ROUNDING_MODE);
    }

    public static BigDecimal annualToMonthlyRate(BigDecimal annualRate) {
        return annualRate.divide(MONTHS_IN_YEAR_PERCENT, CALCULATION_SCALE, ROUNDING_MODE);
    }

}
