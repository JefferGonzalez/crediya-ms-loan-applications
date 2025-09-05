package co.com.pragma.crediya.model.user.gateways;

import co.com.pragma.crediya.model.user.User;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface UserPort {

    Mono<List<User>> getUsersByEmails(Set<String> emails);

}
