package co.com.pragma.crediya.api.config;

import co.com.pragma.crediya.api.LoanApplicationHandler;
import co.com.pragma.crediya.api.RouterRest;
import co.com.pragma.crediya.api.config.security.*;
import co.com.pragma.crediya.api.constants.ApiConstants;
import co.com.pragma.crediya.api.exceptions.handler.CustomAccessDeniedHandler;
import co.com.pragma.crediya.api.exceptions.handler.GlobalExceptionHandler;
import co.com.pragma.crediya.api.mapper.LoanApplicationRestMapper;
import co.com.pragma.crediya.api.validator.ReactiveValidator;
import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.jwt.gateways.JwtProviderPort;
import co.com.pragma.crediya.usecase.loan.report.ApplicationReportUseCase;
import co.com.pragma.crediya.usecase.loan.ApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, LoanApplicationHandler.class})
@WebFluxTest
@Import({
        CorsConfig.class,
        SecurityHeadersConfig.class,
        GlobalExceptionHandler.class,
        CustomAccessDeniedHandler.class,
        SecurityConfig.class
})
class ConfigTest {

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
    private LoanApplicationRestMapper mapper;

    @MockitoBean
    private ApplicationReportUseCase applicationReportUseCase;

    @MockitoBean
    private ApplicationUseCase userUseCase;

    @Mock
    private Jwt mockJwt;

    private static final String FAKE_TOKEN = "fake-jwt-token";

    @BeforeEach
    void setup() {
        when(mockJwt.subject()).thenReturn("1234567890");
        when(mockJwt.identificationNumber()).thenReturn("jhondoe@example.com");

        when(securityUtils.getJwt()).thenReturn(Mono.just(mockJwt));

        when(securityContextRepository.load(any()))
                .thenReturn(Mono.just(new SecurityContextImpl(
                        new UsernamePasswordAuthenticationToken(FAKE_TOKEN, FAKE_TOKEN, List.of(new SimpleGrantedAuthority(DomainConstants.CUSTOMER_ROLE)))
                )));
    }

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.post()
                .uri(ApiConstants.BASE_PATH)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}