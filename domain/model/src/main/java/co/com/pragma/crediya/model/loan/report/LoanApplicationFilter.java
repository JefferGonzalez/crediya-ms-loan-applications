package co.com.pragma.crediya.model.loan.report;

import java.math.BigDecimal;
import java.util.Set;

public record LoanApplicationFilter(
        Set<String> statuses,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Integer minTerm,
        Integer maxTerm,
        String email,
        String loanType,
        int limit,
        int page) {

    public boolean onlyPagination() {
        return (statuses == null || statuses.isEmpty()) &&
                minAmount == null &&
                maxAmount == null &&
                minTerm == null &&
                maxTerm == null &&
                email == null &&
                loanType == null;
    }
}
