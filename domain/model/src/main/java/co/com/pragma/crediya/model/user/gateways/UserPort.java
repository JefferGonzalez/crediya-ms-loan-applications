package co.com.pragma.crediya.model.user.gateways;

import co.com.pragma.crediya.model.user.User;
import reactor.core.publisher.Mono;

public interface UserPort {

    Mono<User> getUserByIdentificationNumber(String identificationNumber);

}
