package co.com.pragma.crediya.model.loan;

import java.math.BigDecimal;

public record ActiveApplication(
        BigDecimal amount,
        Integer term,
        BigDecimal interestRate) {
}