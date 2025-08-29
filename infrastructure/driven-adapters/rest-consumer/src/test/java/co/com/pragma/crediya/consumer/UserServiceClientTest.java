package co.com.pragma.crediya.consumer;

import co.com.pragma.crediya.consumer.mapper.UserExternalMapper;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.user.User;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

    private static MockWebServer mockWebServer;

    private static UserServiceClient userServiceClient;

    @Mock
    private static LoggerPort logger;

    @Mock
    private static UserExternalMapper mapper;

    @BeforeAll
    static void startMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

        userServiceClient = new UserServiceClient(webClient, logger, mapper);
    }

    @Test
    void shouldReturnUserWhenApiRespondsOk() {
        String jsonResponse = """
                {
                  "identificationNumber": "123456789",
                  "email": "john@example.com"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        when(mapper.toDomain(any()))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0);
                    return new User(
                            "123456789",
                            "john@example.com"
                    );
                });

        String userIdentificationNumber = "123456789";
        var result = userServiceClient.getUserByIdentificationNumber(userIdentificationNumber).block();

        assertThat(result).isNotNull();
        assertThat(result.identificationNumber()).isEqualTo(userIdentificationNumber);
        assertThat(result.email()).isEqualTo("john@example.com");
    }

    @Test
    void shouldHandleNotFoundGracefully() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json"));

        Mono<?> result = userServiceClient.getUserByIdentificationNumber("999");

        assertThat(result.onErrorResume(e -> Mono.empty()).block()).isNull();
    }
}
