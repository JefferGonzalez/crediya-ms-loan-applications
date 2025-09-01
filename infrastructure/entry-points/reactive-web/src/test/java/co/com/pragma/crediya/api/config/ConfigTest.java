package co.com.pragma.crediya.api.config;

import co.com.pragma.crediya.api.LoanApplicationHandler;
import co.com.pragma.crediya.api.RouterRest;
import co.com.pragma.crediya.api.exceptions.GlobalExceptionHandler;
import co.com.pragma.crediya.api.mapper.LoanApplicationRestMapper;
import co.com.pragma.crediya.api.validator.ReactiveValidator;
import co.com.pragma.crediya.usecase.loan.ApplicationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@ContextConfiguration(classes = {RouterRest.class, LoanApplicationHandler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class, GlobalExceptionHandler.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveValidator reactiveValidator;

    @MockitoBean
    private LoanApplicationRestMapper mapper;

    @MockitoBean
    private ApplicationUseCase userUseCase;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.post()
                .uri("/api/v1/loan-applications")
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