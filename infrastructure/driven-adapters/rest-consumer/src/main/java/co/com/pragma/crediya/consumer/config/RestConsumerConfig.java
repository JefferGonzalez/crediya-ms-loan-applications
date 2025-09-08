package co.com.pragma.crediya.consumer.config;

import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
@RequiredArgsConstructor
public class RestConsumerConfig {

    private final RestConsumerProperties properties;

    private final LoggerPort logger;

    @Bean
    public WebClient getWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(properties.getUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(getClientHttpConnector())
                .filter(addAuthToken())
                .filter(logRequests())
                .build();
    }

    private ClientHttpConnector getClientHttpConnector() {
        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, properties.getTimeout())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(properties.getTimeout(), MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(properties.getTimeout(), MILLISECONDS));
                }));
    }

    private ExchangeFilterFunction addAuthToken() {
        return (clientRequest, next) ->
                ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .map(Authentication::getCredentials)
                        .cast(String.class)
                        .map(token -> ClientRequest.from(clientRequest)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .build())
                        .defaultIfEmpty(clientRequest)
                        .flatMap(next::exchange)
                        .onErrorResume(ex -> {
                            logger.warn("Could not get JWT token, proceeding without auth header: {}", ex.getMessage());

                            return next.exchange(clientRequest);
                        });
    }

    private ExchangeFilterFunction logRequests() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            logger.info("Outbound Request: {} {}", request.method(), request.url());

            return Mono.just(request);
        });
    }

}
