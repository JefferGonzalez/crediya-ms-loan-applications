package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.api.mapper.LoanApplicationMapper;
import co.com.pragma.crediya.api.validator.ReactiveValidator;
import co.com.pragma.crediya.model.loan.exceptions.EmptyRequestBodyException;
import co.com.pragma.crediya.usecase.loan.ApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LoanApplicationHandler {

    private final ApplicationUseCase applicationUseCase;

    private final ReactiveValidator reactiveValidator;

    public Mono<ServerResponse> listenPOSTSaveLoanApplicationUserCase(ServerRequest request) {
        return request.bodyToMono(SaveLoanApplicationRequest.class)
                .switchIfEmpty(Mono.error(new EmptyRequestBodyException()))
                .flatMap(reactiveValidator::validate)
                .map(LoanApplicationMapper::toDomain)
                .flatMap(applicationUseCase::execute)
                .map(LoanApplicationMapper::toResponse)
                .flatMap(storedLoanApplication ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(storedLoanApplication)
                );
    }
}
