package co.com.pragma.crediya.usecase.loan.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationCalculatorUtilsTest {

    @Test
    @DisplayName("calculateMonthlyPayment() should return correct value for non-zero interest rate")
    void calculateMonthlyPayment_withInterestRate() {
        BigDecimal amount = BigDecimal.valueOf(1200);
        BigDecimal interestRate = BigDecimal.valueOf(0.05);
        int term = 12;

        BigDecimal result = ApplicationCalculatorUtils.calculateMonthlyPayment(amount, interestRate, term);

        assertThat(result).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateMonthlyPayment() should return correct value for zero interest rate")
    void calculateMonthlyPayment_zeroInterestRate() {
        BigDecimal amount = BigDecimal.valueOf(1200);
        BigDecimal interestRate = BigDecimal.ZERO;
        int term = 12;

        BigDecimal result = ApplicationCalculatorUtils.calculateMonthlyPayment(amount, interestRate, term);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP));
    }

}
