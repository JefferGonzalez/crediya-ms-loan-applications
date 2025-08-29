package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.gateways.TypeRepository;
import co.com.pragma.crediya.r2dbc.mapper.LoanTypeMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class LoanTypeRepositoryAdapter implements TypeRepository {

    private final LoanTypeReactiveRepository loanTypeReactiveRepository;

    private final LoanTypeMapper loanTypeMapper;

    public LoanTypeRepositoryAdapter(LoanTypeReactiveRepository loanTypeReactiveRepository, LoanTypeMapper loanTypeMapper) {
        this.loanTypeReactiveRepository = loanTypeReactiveRepository;
        this.loanTypeMapper = loanTypeMapper;
    }

    @Override
    public Flux<Type> findAll() {
        return loanTypeReactiveRepository.findAll()
                .map(loanTypeMapper::toDomain);
    }

    @Override
    public Mono<Type> findByName(String name) {
        return loanTypeReactiveRepository.findByName(name)
                .map(loanTypeMapper::toDomain);
    }
}
