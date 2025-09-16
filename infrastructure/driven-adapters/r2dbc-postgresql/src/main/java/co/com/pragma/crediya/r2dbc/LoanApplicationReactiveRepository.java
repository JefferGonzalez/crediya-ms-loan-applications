package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.r2dbc.entity.LoanApplicationEntity;
import co.com.pragma.crediya.r2dbc.projection.LoanAmortizationProjection;
import co.com.pragma.crediya.r2dbc.projection.LoanApplicationProjection;
import co.com.pragma.crediya.r2dbc.reports.LoanApplicationCustomRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LoanApplicationReactiveRepository
        extends ReactiveCrudRepository<LoanApplicationEntity, UUID>,
        ReactiveQueryByExampleExecutor<LoanApplicationEntity>,
        LoanApplicationCustomRepository {

    @Query("""
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
                JOIN loan_status S ON LA.status_id = S.id
                WHERE
                    S.NAME IN ('UNDER REVIEW', 'MANUAL REVIEW', 'REJECTED')
                LIMIT :limit OFFSET :offset;
            """)
    Flux<LoanApplicationProjection> queryLoanApplications(int limit, int offset);

    @Query("""
                SELECT COUNT(*)
                FROM loan_application AS LA
                JOIN loan_type AS T ON T.id = LA.type_id
                JOIN loan_status S ON LA.status_id = S.id
                WHERE S.name IN ('UNDER REVIEW', 'MANUAL REVIEW', 'REJECTED')
            """)
    Mono<Long> countLoanApplications();

    @Query("""
            SELECT
                LA.amount,
                LA.term,
                T.interest_rate
            FROM loan_application AS LA
            JOIN loan_type AS T ON T.id = LA.type_id
            JOIN loan_status S ON LA.status_id = S.id AND S.name = 'APPROVED'
            WHERE LA.identification_number = :identificationNumber
            """)
    Flux<LoanAmortizationProjection> queryActiveLoansByIdentificationNumber(String identificationNumber);

}
