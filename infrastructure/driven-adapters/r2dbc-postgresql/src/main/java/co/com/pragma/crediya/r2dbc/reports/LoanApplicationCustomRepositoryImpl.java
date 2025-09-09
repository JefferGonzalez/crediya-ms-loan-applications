package co.com.pragma.crediya.r2dbc.reports;

import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.r2dbc.projection.LoanApplicationProjection;
import co.com.pragma.crediya.r2dbc.reports.utils.LoanApplicationQueryBuilder;
import co.com.pragma.crediya.r2dbc.reports.utils.SqlWithParams;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class LoanApplicationCustomRepositoryImpl implements LoanApplicationCustomRepository {

    private final DatabaseClient client;

    private final LoanApplicationQueryBuilder builder = new LoanApplicationQueryBuilder();

    @Override
    public Flux<LoanApplicationProjection> findLoanApplications(LoanApplicationFilter filter) {
        SqlWithParams where = builder.buildWhereClause(filter);

        String sql = """
                    SELECT
                        LA.id,
                        LA.amount,
                        LA.term,
                        LA.email,
                        T.name AS type,
                        T.interest_rate,
                        S.name AS status
                    FROM loan_application AS LA
                    JOIN loan_type AS T ON T.id = LA.type_id
                    JOIN loan_status S ON S.id = LA.status_id
                """ + where.sql() + " LIMIT :limit OFFSET :offset";

        Map<String, Object> params = new HashMap<>(where.params());
        params.put("limit", filter.limit());
        params.put("offset", (filter.page() - 1) * filter.limit());

        return client.sql(sql)
                .bindValues(params)
                .map(this::mapRowToDto)
                .all();
    }

    @Override
    public Mono<Long> countLoanApplications(LoanApplicationFilter filter) {
        SqlWithParams where = builder.buildWhereClause(filter);

        String sql = """
                    SELECT
                        COUNT(*) AS total
                    FROM loan_application AS LA
                    JOIN loan_type AS T ON T.id = LA.type_id
                    JOIN loan_status S ON S.id = LA.status_id
                """ + where.sql();

        return client.sql(sql)
                .bindValues(where.params())
                .map(row -> row.get("total", Long.class))
                .one()
                .defaultIfEmpty(0L);
    }

    private LoanApplicationProjection mapRowToDto(Row row, RowMetadata meta) {
        return new LoanApplicationProjection(
                row.get("id", UUID.class),
                row.get("amount", BigDecimal.class),
                row.get("term", Integer.class),
                row.get("email", String.class),
                row.get("type", String.class),
                row.get("interest_rate", BigDecimal.class),
                row.get("status", String.class)
        );
    }

}
