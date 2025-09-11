package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.loan.report.ApplicationReport;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.r2dbc.mapper.LoanApplicationMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class LoanApplicationRepositoryAdapter implements ApplicationRepository {

    private final LoanApplicationReactiveRepository loanApplicationReactiveRepository;

    private final LoanApplicationMapper loanApplicationMapper;

    public LoanApplicationRepositoryAdapter(LoanApplicationReactiveRepository loanApplicationReactiveRepository, LoanApplicationMapper loanApplicationMapper) {
        this.loanApplicationReactiveRepository = loanApplicationReactiveRepository;
        this.loanApplicationMapper = loanApplicationMapper;
    }

    @Override
    public Mono<Application> findById(UUID id) {
        return loanApplicationReactiveRepository.findById(id)
                .map(loanApplicationMapper::toDomain);
    }

    @Override
    public Mono<Application> save(Application application) {
        return loanApplicationReactiveRepository.save(loanApplicationMapper.toEntity(application))
                .map(loanApplicationMapper::toDomain);
    }

    @Override
    public Flux<ApplicationReport> findApplicationsReport(int limit, int page) {
        int offset = (page - 1) * limit;

        return loanApplicationReactiveRepository.queryLoanApplications(limit, offset)
                .map(loanApplicationMapper::toDomain);
    }

    @Override
    public Mono<Long> countLoanApplications() {
        return loanApplicationReactiveRepository.countLoanApplications();
    }

    @Override
    public Flux<ApplicationReport> findApplicationsReport(LoanApplicationFilter filter) {
        return loanApplicationReactiveRepository.findLoanApplications(filter)
                .map(loanApplicationMapper::toDomain);
    }

    @Override
    public Mono<Long> countLoanApplications(LoanApplicationFilter filter) {
        return loanApplicationReactiveRepository.countLoanApplications(filter);
    }

}
