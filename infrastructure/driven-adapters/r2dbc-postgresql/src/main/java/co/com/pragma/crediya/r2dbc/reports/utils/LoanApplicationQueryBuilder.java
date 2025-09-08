package co.com.pragma.crediya.r2dbc.reports.utils;

import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;

import java.util.HashMap;
import java.util.Map;

public class LoanApplicationQueryBuilder {

    public SqlWithParams buildWhereClause(LoanApplicationFilter filter) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            where.append(" AND S.name IN (:statuses)");
            params.put("statuses", filter.statuses());
        }

        if (filter.minAmount() != null) {
            where.append(" AND LA.amount >= :minAmount");
            params.put("minAmount", filter.minAmount());
        }

        if (filter.maxAmount() != null) {
            where.append(" AND LA.amount <= :maxAmount");
            params.put("maxAmount", filter.maxAmount());
        }

        if (filter.minTerm() != null) {
            where.append(" AND LA.term >= :minTerm");
            params.put("minTerm", filter.minTerm());
        }

        if (filter.maxTerm() != null) {
            where.append(" AND LA.term <= :maxTerm");
            params.put("maxTerm", filter.maxTerm());
        }

        if (filter.email() != null) {
            where.append(" AND LA.email = :email");
            params.put("email", filter.email());
        }

        if (filter.loanType() != null) {
            where.append(" AND T.name = :loanType");
            params.put("loanType", filter.loanType());
        }

        return new SqlWithParams(where.toString(), params);
    }
}
