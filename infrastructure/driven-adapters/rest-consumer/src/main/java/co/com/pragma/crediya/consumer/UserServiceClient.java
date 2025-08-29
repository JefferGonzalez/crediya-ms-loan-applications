package co.com.pragma.crediya.consumer;

import co.com.pragma.crediya.consumer.dto.UserEmailResponse;
import co.com.pragma.crediya.consumer.mapper.UserExternalMapper;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.user.User;
import co.com.pragma.crediya.model.user.constants.UserErrorMessages;
import co.com.pragma.crediya.model.user.constants.UserServiceMessages;
import co.com.pragma.crediya.model.user.exceptions.UserGatewayException;
import co.com.pragma.crediya.model.user.exceptions.UserNotFoundException;
import co.com.pragma.crediya.model.user.gateways.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceClient implements UserPort {

    private final WebClient webClient;

    private final LoggerPort logger;

    private final UserExternalMapper userExternalMapper;

    @Override
    public Mono<User> getUserByIdentificationNumber(String identificationNumber) {
        return webClient
                .get()
                .uri("/api/v1/users/{identificationNumber}", identificationNumber)
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND),
                        clientResponse -> {
                            UserNotFoundException ex = new UserNotFoundException();
                            logger.error(UserErrorMessages.USER_NOT_FOUND, ex);

                            return Mono.error(ex);
                        }
                )
                .onStatus(httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    UserGatewayException ex = new UserGatewayException(UserServiceMessages.RESPONSE_ERROR);
                                    logger.warn("Error from user service. Payload: {}", error);

                                    return Mono.error(ex);
                                }))
                .bodyToMono(UserEmailResponse.class)
                .map(userExternalMapper::toDomain)
                .doOnSuccess(user -> logger.info("User found: {}", user.identificationNumber()))
                .onErrorMap(
                        ex -> !(ex instanceof UserNotFoundException || ex instanceof UserGatewayException),
                        ex -> {
                            logger.error(UserServiceMessages.COMMUNICATION_FAILED, ex);

                            return new UserGatewayException(UserServiceMessages.COMMUNICATION_FAILED);
                        }
                );
    }

}
