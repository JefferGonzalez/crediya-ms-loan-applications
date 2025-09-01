package co.com.pragma.crediya.consumer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "adapters.rest-consumer")
public class RestConsumerProperties {

    private String url;

    private int timeout = 5000;

}
