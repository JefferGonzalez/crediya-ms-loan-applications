package co.com.pragma.crediya.model.loan;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ApplicationRiskEvaluation(
        UUID id,
        String type,
        BigDecimal amount,
        int term,
        BigDecimal interestRate,
        BigDecimal baseSalary,
        List<ActiveApplication> activeApplications) {
}
