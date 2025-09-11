package co.com.pragma.crediya.model.loan.report;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerApplication(
        UUID id,
        String email,
        BigDecimal baseSalary,
        BigDecimal monthlyPayment,
        String type,
        BigDecimal amount,
        int term,
        BigDecimal interestRate,
        String status) {
}
