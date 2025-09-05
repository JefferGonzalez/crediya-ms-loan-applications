package co.com.pragma.crediya.model.loan.report;

import java.math.BigDecimal;

public record CustomerApplication(
        String email,
        BigDecimal baseSalary,
        BigDecimal totalMonthlyDebt,
        String type,
        BigDecimal amount,
        int term,
        BigDecimal interestRate,
        String status) {
}
