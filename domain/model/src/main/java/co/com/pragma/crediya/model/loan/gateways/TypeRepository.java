package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.Type;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TypeRepository {

    Flux<Type> findAll();

    Mono<Type> findByName(String name);

}
