package co.com.pragma.crediya.model.loan;

import java.math.BigDecimal;
import java.util.UUID;

public record Type(
        UUID id,
        String name,
        BigDecimal minimumAmount,
        BigDecimal maximumAmount,
        BigDecimal interestRate,
        Boolean automaticValidation) {
}
