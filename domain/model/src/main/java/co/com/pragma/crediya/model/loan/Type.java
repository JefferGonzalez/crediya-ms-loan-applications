package co.com.pragma.crediya.model.loan;

import java.math.BigDecimal;
import java.util.UUID;

public record Type(
        UUID id,
        String name,
        BigDecimal minimumAmount,
        BigDecimal maximumAmount,
        int minimumTerm,
        int maximumTerm,
        BigDecimal interestRate,
        Boolean automaticValidation) {
}
