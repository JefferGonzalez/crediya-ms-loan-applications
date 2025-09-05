package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.report.ApplicationReport;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {

    Mono<Application> save(Application application);

    Flux<ApplicationReport> findApplicationsReport(int limit, int page);

    Mono<Long> countLoanApplications();

    Flux<ApplicationReport> findApplicationsReport(LoanApplicationFilter filter);

    Mono<Long> countLoanApplications(LoanApplicationFilter filter);

}
