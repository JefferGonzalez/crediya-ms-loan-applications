package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.config.security.JwtAuthenticationManager;
import co.com.pragma.crediya.api.config.security.SecurityConfig;
import co.com.pragma.crediya.api.config.security.SecurityContextRepository;
import co.com.pragma.crediya.api.config.security.SecurityUtils;
import co.com.pragma.crediya.api.constants.ApiConstants;
import co.com.pragma.crediya.api.dto.LoanApplicationResponse;
import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.api.exceptions.handler.CustomAccessDeniedHandler;
import co.com.pragma.crediya.api.exceptions.handler.GlobalExceptionHandler;
import co.com.pragma.crediya.api.mapper.LoanApplicationRestMapper;
import co.com.pragma.crediya.api.validator.ReactiveValidator;
import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.jwt.gateways.JwtProviderPort;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.usecase.loan.report.ApplicationReportUseCase;
import co.com.pragma.crediya.usecase.loan.ApplicationUseCase;
import jakarta.validation.Validator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        RouterRest.class,
        LoanApplicationHandler.class,
        GlobalExceptionHandler.class,
        CustomAccessDeniedHandler.class,
        SecurityConfig.class,
})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private JwtProviderPort jwtProviderPort;

    @MockitoBean
    private JwtAuthenticationManager jwtAuthenticationManager;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @MockitoBean
    private ReactiveValidator reactiveValidator;

    @MockitoBean
    private SecurityUtils securityUtils;

    @MockitoBean
    private LoanApplicationRestMapper loanApplicationMapper;

    @MockitoBean
    private Validator validator;

    @MockitoBean
    private LoggerPort logger;

    @MockitoBean
    private ApplicationReportUseCase applicationReportUseCase;

    @MockitoBean
    private ApplicationUseCase applicationUseCase;

    @Mock
    private Jwt mockJwt;

    private SaveLoanApplicationRequest request;

    private final UUID applicationId = UUID.randomUUID();

    private static final String FAKE_TOKEN = "fake-jwt-token";

    @BeforeEach
    void setup() {
        request = SaveLoanApplicationRequest.builder()
                .amount("4000000")
                .term("12")
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

        when(mockJwt.subject()).thenReturn(application.email());
        when(mockJwt.identificationNumber()).thenReturn(application.identificationNumber());

        when(securityUtils.getJwt()).thenReturn(Mono.just(mockJwt));

        when(reactiveValidator.validate(any())).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        when(securityContextRepository.load(any()))
                .thenReturn(Mono.just(new SecurityContextImpl(
                        new UsernamePasswordAuthenticationToken(FAKE_TOKEN, FAKE_TOKEN, List.of(new SimpleGrantedAuthority(DomainConstants.CUSTOMER_ROLE)))
                )));

        when(loanApplicationMapper.toDomain(any(SaveLoanApplicationRequest.class))).thenReturn(application);

        when(applicationUseCase.save(any(Application.class), any(Jwt.class))).thenReturn(Mono.just(application));

        when(loanApplicationMapper.toResponse(any(Application.class))).thenReturn(response);
    }

    @Test
    void createLoanApplication_shouldReturnCreated_whenValidRequest() {
        webTestClient.post()
                .uri(ApiConstants.LOAN_APPLICATIONS_PATH)
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
                .uri(ApiConstants.LOAN_APPLICATIONS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("")
                .exchange()
                .expectStatus().isBadRequest();
    }

}
