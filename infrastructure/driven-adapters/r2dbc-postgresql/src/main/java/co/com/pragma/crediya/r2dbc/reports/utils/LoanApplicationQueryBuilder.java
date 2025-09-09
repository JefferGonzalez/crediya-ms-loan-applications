package co.com.pragma.crediya.r2dbc.reports.utils;

import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class LoanApplicationQueryBuilder {

    private static final String BASE_WHERE = " WHERE 1=1 ";

    public SqlWithParams buildWhereClause(LoanApplicationFilter filter) {
        if (filter == null) {
            return new SqlWithParams(BASE_WHERE, new HashMap<>());
        }

        StringBuilder where = new StringBuilder(BASE_WHERE);
        Map<String, Object> params = new HashMap<>();

        addStatusFilter(where, params, filter);
        addLoanTypeFilter(where, params, filter);
        addEmailFilter(where, params, filter);
        addAmountFilters(where, params, filter);
        addTermFilters(where, params, filter);

        return new SqlWithParams(where.toString(), params);
    }

    private void addStatusFilter(StringBuilder where, Map<String, Object> params, LoanApplicationFilter filter) {
        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            where.append(" AND LOWER(S.name) IN (:statuses)");
            params.put("statuses", filter.statuses().stream()
                    .map(String::toLowerCase)
                    .toList());
        }
    }

    private void addLoanTypeFilter(StringBuilder where, Map<String, Object> params, LoanApplicationFilter filter) {
        if (StringUtils.hasText(filter.loanType())) {
            where.append(" AND LOWER(T.name) = :loanType");
            params.put("loanType", filter.loanType().trim().toLowerCase());
        }
    }

    private void addEmailFilter(StringBuilder where, Map<String, Object> params, LoanApplicationFilter filter) {
        if (StringUtils.hasText(filter.email())) {
            where.append(" AND LOWER(LA.email) = :email");
            params.put("email", filter.email().trim().toLowerCase());
        }
    }

    private void addAmountFilters(StringBuilder where, Map<String, Object> params, LoanApplicationFilter filter) {
        if (filter.minAmount() != null) {
            where.append(" AND LA.amount >= :minAmount");
            params.put("minAmount", filter.minAmount());
        }

        if (filter.maxAmount() != null) {
            where.append(" AND LA.amount <= :maxAmount");
            params.put("maxAmount", filter.maxAmount());
        }
    }

    private void addTermFilters(StringBuilder where, Map<String, Object> params, LoanApplicationFilter filter) {
        if (filter.minTerm() != null) {
            where.append(" AND LA.term >= :minTerm");
            params.put("minTerm", filter.minTerm());
        }

        if (filter.maxTerm() != null) {
            where.append(" AND LA.term <= :maxTerm");
            params.put("maxTerm", filter.maxTerm());
        }
    }

}
