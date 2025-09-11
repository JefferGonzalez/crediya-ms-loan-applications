package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.Type;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TypeRepository {

    Mono<Type> findById(UUID id);

    Mono<Type> findByName(String name);

}
