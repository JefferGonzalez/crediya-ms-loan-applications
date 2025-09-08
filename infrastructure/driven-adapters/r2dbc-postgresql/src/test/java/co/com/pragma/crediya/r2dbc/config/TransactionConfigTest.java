package co.com.pragma.crediya.r2dbc.config;

import co.com.pragma.crediya.model.transaction.gateways.TransactionalPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TransactionConfigTest {

    @Mock
    private ReactiveTransactionManager transactionManager;

    @Mock
    private ReactiveTransaction reactiveTransaction;

    private TransactionalPort transactionalPort;

    @BeforeEach
    void setUp() {
        TransactionConfig transactionConfig = new TransactionConfig();
        transactionalPort = transactionConfig.transactionalPort(transactionManager);

        lenient().when(transactionManager.getReactiveTransaction(any())).thenReturn(Mono.just(reactiveTransaction));
        lenient().when(transactionManager.commit(any())).thenReturn(Mono.empty());
        lenient().when(transactionManager.rollback(any())).thenReturn(Mono.empty());
    }

    @Test
    void transactionalMono_shouldWrapInTransaction() {
        Mono<String> mono = Mono.just("test");

        StepVerifier.create(transactionalPort.transactional(mono))
                .expectNext("test")
                .verifyComplete();
    }

    @Test
    void transactionalFlux_shouldWrapInTransaction() {
        Flux<String> flux = Flux.just("a", "b");

        StepVerifier.create(transactionalPort.transactional(flux))
                .expectNext("a", "b")
                .verifyComplete();
    }

}