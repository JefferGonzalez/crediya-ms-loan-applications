package co.com.pragma.crediya.model.loan.report;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplicationReport(
        UUID id,
        BigDecimal amount,
        int term,
        String email,
        String type,
        BigDecimal interestRate,
        String status) {
}
