package co.com.pragma.crediya.model.loan;

import java.math.BigDecimal;

public record PaymentDetail(
        int month,
        BigDecimal installment,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal balance) {
}
