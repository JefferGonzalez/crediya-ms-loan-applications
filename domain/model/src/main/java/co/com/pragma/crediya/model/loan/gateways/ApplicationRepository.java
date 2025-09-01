package co.com.pragma.crediya.model.loan.gateways;

import co.com.pragma.crediya.model.loan.Application;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {

    Mono<Application> save(Application application);

}
