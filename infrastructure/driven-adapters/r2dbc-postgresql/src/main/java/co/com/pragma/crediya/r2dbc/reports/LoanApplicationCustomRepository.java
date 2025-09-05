package co.com.pragma.crediya.r2dbc.reports;

import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.r2dbc.projection.LoanApplicationProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanApplicationCustomRepository {

    Flux<LoanApplicationProjection> findLoanApplications(LoanApplicationFilter filter);

    Mono<Long> countLoanApplications(LoanApplicationFilter filter);

}

