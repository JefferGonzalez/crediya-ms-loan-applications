package co.com.pragma.crediya.model;

import java.math.BigDecimal;

public record StatusChange(
        String status,
        String loanType,
        BigDecimal amount) {
}
