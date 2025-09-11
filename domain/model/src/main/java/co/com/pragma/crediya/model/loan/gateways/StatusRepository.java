package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.Status;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StatusRepository {

    Mono<Status> findById(UUID id);

    Mono<Status> findByName(String name);

}
