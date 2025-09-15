package co.com.pragma.crediya.consumer;

import co.com.pragma.crediya.consumer.dto.UserResponse;
import co.com.pragma.crediya.consumer.mapper.UserExternalMapper;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

    private static MockWebServer mockWebServer;

    private static UserServiceClient userServiceClient;

    @Mock
    private static LoggerPort logger;

    @Mock
    private static UserExternalMapper mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void getUsersByEmails_shouldReturnMappedUsers() throws JsonProcessingException {
        UserResponse john = new UserResponse(null,"john@example.com", BigDecimal.valueOf(10000));
        UserResponse jane = new UserResponse(null, "jane@example.com", BigDecimal.valueOf(20000));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(List.of(john, jane)))
                .addHeader("Content-Type", "application/json"));

        when(mapper.toDomain(any(UserResponse.class)))
                .thenAnswer(invocation -> {
                    UserResponse dto = invocation.getArgument(0);
                    return new User(null, dto.getEmail(), dto.getBaseSalary());
                });

        Set<String> emails = Set.of(john.getEmail(), jane.getEmail());
        StepVerifier.create(userServiceClient.getUsersByEmails(emails))
                .assertNext(users -> {
                    assertThat(users).hasSize(2);
                    assertThat(users.get(0).email()).isEqualTo(john.getEmail());
                    assertThat(users.get(1).email()).isEqualTo(jane.getEmail());
                })
                .verifyComplete();
    }

}
