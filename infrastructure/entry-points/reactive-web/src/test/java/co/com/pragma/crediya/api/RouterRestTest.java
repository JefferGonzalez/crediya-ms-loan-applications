package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.LoanApplicationResponse;
import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.api.mapper.LoanApplicationRestMapper;
import co.com.pragma.crediya.api.validator.ReactiveValidator;
import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.usecase.loan.ApplicationUseCase;
import jakarta.validation.Validator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, LoanApplicationHandler.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveValidator reactiveValidator;

    @MockitoBean
    private LoanApplicationRestMapper loanApplicationMapper;

    @MockitoBean
    private Validator validator;

    @MockitoBean
    private LoggerPort logger;

    @MockitoBean
    private ApplicationUseCase applicationUseCase;

    private SaveLoanApplicationRequest request;

    private final UUID applicationId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        request = SaveLoanApplicationRequest.builder()
                .amount("4000000")
                .term("12")
                .identificationNumber("1234567890")
                .type(DomainConstants.MICROCREDIT)
                .build();

        Application application = new Application(applicationId, new BigDecimal(4000000), 12, "1234567890", "jhondoe@example.com", null, null);

        LoanApplicationResponse response = LoanApplicationResponse.builder()
                .id(applicationId)
                .amount(new BigDecimal(4000000))
                .term(12)
                .type(DomainConstants.MICROCREDIT)
                .status(DomainConstants.DEFAULT_PENDING_STATUS)
                .build();

        when(reactiveValidator.validate(any())).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        when(loanApplicationMapper.toDomain(any(SaveLoanApplicationRequest.class))).thenReturn(application);

        when(applicationUseCase.save(any(Application.class))).thenReturn(Mono.just(application));

        when(loanApplicationMapper.toResponse(any(Application.class))).thenReturn(response);
    }

    @Test
    void createLoanApplication_shouldReturnCreated_whenValidRequest() {
        webTestClient.post()
                .uri("/api/v1/loan-applications")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LoanApplicationResponse.class)
                .value(response -> {
                    Assertions.assertThat(response).isInstanceOf(LoanApplicationResponse.class);
                    Assertions.assertThat(response.getId()).isEqualTo(applicationId);
                    Assertions.assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(4000000));
                    Assertions.assertThat(response.getTerm()).isEqualTo(12);
                    Assertions.assertThat(response.getType()).isEqualTo(DomainConstants.MICROCREDIT);
                    Assertions.assertThat(response.getStatus()).isEqualTo(DomainConstants.DEFAULT_PENDING_STATUS);
                });
    }

    @Test
    void createLoanApplication_shouldReturnError_whenBodyEmpty() {
        webTestClient.post()
                .uri("/api/v1/loan-applications")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("")
                .exchange()
                .expectStatus().isBadRequest();
    }

}
