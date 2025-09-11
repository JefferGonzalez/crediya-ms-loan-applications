package co.com.pragma.crediya.config;

import co.com.pragma.crediya.model.jwt.gateways.JwtProviderPort;
import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.notification.gateways.NotificationPort;
import co.com.pragma.crediya.model.loan.gateways.StatusRepository;
import co.com.pragma.crediya.model.loan.gateways.TypeRepository;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.transaction.gateways.TransactionalPort;
import co.com.pragma.crediya.model.user.gateways.UserPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public ApplicationRepository applicationRepository() {
            return Mockito.mock(ApplicationRepository.class);
        }

        @Bean
        public StatusRepository statusRepository() {
            return Mockito.mock(StatusRepository.class);
        }

        @Bean
        public TypeRepository typeRepository() {
            return Mockito.mock(TypeRepository.class);
        }

        @Bean
        public UserPort userPort() {
            return Mockito.mock(UserPort.class);
        }

        @Bean
        public LoggerPort loggerPort() {
            return Mockito.mock(LoggerPort.class);
        }

        @Bean
        public TransactionalPort transactionalPort() {
            return Mockito.mock(TransactionalPort.class);
        }

        @Bean
        public JwtProviderPort jwtProvider() {
            return Mockito.mock(JwtProviderPort.class);
        }

        @Bean
        public NotificationPort notificationPort() {
            return Mockito.mock(NotificationPort.class);
        }

    }
}