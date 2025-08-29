package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.gateways.StatusRepository;
import co.com.pragma.crediya.r2dbc.mapper.LoanStatusMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class LoanStatusRepositoryAdapter implements StatusRepository {

    private final LoanStatusReactiveRepository loanStatusReactiveRepository;

    private final LoanStatusMapper loanStatusMapper;

    public LoanStatusRepositoryAdapter(LoanStatusReactiveRepository loanStatusReactiveRepository, LoanStatusMapper loanStatusMapper) {
        this.loanStatusReactiveRepository = loanStatusReactiveRepository;
        this.loanStatusMapper = loanStatusMapper;
    }

    @Override
    public Mono<Status> findByName(String name) {
        return loanStatusReactiveRepository.findByName(name)
                .map(loanStatusMapper::toDomain);
    }
}
