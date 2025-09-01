package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.r2dbc.entity.LoanStatusEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LoanStatusReactiveRepository
        extends ReactiveCrudRepository<LoanStatusEntity, UUID>, ReactiveQueryByExampleExecutor<LoanStatusEntity> {

    Mono<LoanStatusEntity> findByName(String name);

}
