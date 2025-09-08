package co.com.pragma.crediya.consumer.config;

import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class RestConsumerConfigTest {

    private RestConsumerProperties properties;

    private RestConsumerConfig restConsumerConfig;

    private LoggerPort loggerPort;

    @BeforeEach
    void setUp() {
        properties = new RestConsumerProperties();
        ReflectionTestUtils.setField(properties, "url", "http://localhost:8080");
        ReflectionTestUtils.setField(properties, "timeout", 5000);
        restConsumerConfig = new RestConsumerConfig(properties, loggerPort);
    }

    @Test
    void shouldCreateWebClientBeanWithCustomValues() {
        WebClient webClient = restConsumerConfig.getWebClient(WebClient.builder());

        assertThat(webClient).isNotNull();
        assertThat(properties.getUrl()).isEqualTo("http://localhost:8080");
        assertThat(properties.getTimeout()).isEqualTo(5000);
    }
}

