package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.Status;
import reactor.core.publisher.Mono;

public interface StatusRepository {

    Mono<Status> findByName(String name);

}
