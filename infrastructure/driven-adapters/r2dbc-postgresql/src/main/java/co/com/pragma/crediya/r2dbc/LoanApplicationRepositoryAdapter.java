package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.r2dbc.mapper.LoanApplicationMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class LoanApplicationRepositoryAdapter implements ApplicationRepository {

    private final LoanApplicationReactiveRepository loanApplicationReactiveRepository;

    private final LoanApplicationMapper loanApplicationMapper;

    public LoanApplicationRepositoryAdapter(LoanApplicationReactiveRepository loanApplicationReactiveRepository, LoanApplicationMapper loanApplicationMapper) {
        this.loanApplicationReactiveRepository = loanApplicationReactiveRepository;
        this.loanApplicationMapper = loanApplicationMapper;
    }

    @Override
    public Mono<Application> save(Application application) {
        return loanApplicationReactiveRepository.save(loanApplicationMapper.toEntity(application))
                .map(loanApplicationMapper::toDomain);
    }
}
