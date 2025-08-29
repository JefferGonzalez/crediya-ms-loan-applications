package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.Type;
import reactor.core.publisher.Mono;

public interface TypeRepository {

    Mono<Type> findByName(String name);

}
