package co.com.pragma.crediya.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Crediya Loan Applications Microservice")
                        .version("1.0.0")
                        .description("This is the API for Crediya Loan Applications Microservice"));
    }
}
