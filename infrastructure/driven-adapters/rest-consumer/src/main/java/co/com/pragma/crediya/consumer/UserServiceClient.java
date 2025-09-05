package co.com.pragma.crediya.consumer;

import co.com.pragma.crediya.consumer.dto.EmailsRequest;
import co.com.pragma.crediya.consumer.dto.UserResponse;
import co.com.pragma.crediya.consumer.mapper.UserExternalMapper;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.user.User;
import co.com.pragma.crediya.model.user.constants.UserServiceMessages;
import co.com.pragma.crediya.model.user.exceptions.UserGatewayException;
import co.com.pragma.crediya.model.user.gateways.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceClient implements UserPort {

    private final WebClient webClient;

    private final LoggerPort logger;

    private final UserExternalMapper userExternalMapper;

    @Override
    public Mono<List<User>> getUsersByEmails(Set<String> emails) {
        EmailsRequest request = new EmailsRequest(new ArrayList<>(emails));


        return webClient
                .post()
                .uri("/api/v1/users/search")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    UserGatewayException ex = new UserGatewayException(UserServiceMessages.RESPONSE_ERROR);
                                    logger.warn("Error from user service. Payload: {}", error);

                                    return Mono.error(ex);
                                })
                )
                .bodyToFlux(UserResponse.class)
                .map(userExternalMapper::toDomain)
                .collectList()
                .doOnSuccess(users -> logger.info("Users found: {}", users.size()))
                .onErrorMap(
                        ex -> !(ex instanceof UserGatewayException),
                        ex -> {
                            logger.error(UserServiceMessages.COMMUNICATION_FAILED, ex);

                            return new UserGatewayException(UserServiceMessages.COMMUNICATION_FAILED);
                        }
                );
    }

}
