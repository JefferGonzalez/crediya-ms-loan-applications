package co.com.pragma.crediya.r2dbc.projection;

import java.math.BigDecimal;

public record LoanAmortizationProjection(
        BigDecimal amount,
        Integer term,
        BigDecimal interestRate) {
}
