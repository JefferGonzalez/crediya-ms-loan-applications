package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.r2dbc.entity.LoanStatusEntity;
import co.com.pragma.crediya.r2dbc.mapper.LoanStatusMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanStatusRepositoryAdapterTest {

    @Mock
    private LoanStatusReactiveRepository reactiveRepository;

    @Mock
    private LoanStatusMapper mapper;

    @InjectMocks
    private LoanStatusRepositoryAdapter adapter;

    private Status status;

    private LoanStatusEntity statusEntity;

    @BeforeEach
    void setUp() {
        status = new Status(UUID.randomUUID(), DomainConstants.DEFAULT_PENDING_STATUS, "Application received, under evaluation");
        statusEntity = new LoanStatusEntity(status.id(), status.name(), status.description());
    }

    @Test
    void findByNameSuccessfully() {
        when(reactiveRepository.findByName(DomainConstants.DEFAULT_PENDING_STATUS)).thenReturn(Mono.just(statusEntity));
        when(mapper.toDomain(statusEntity)).thenReturn(status);

        StepVerifier.create(adapter.findByName(DomainConstants.DEFAULT_PENDING_STATUS))
                .expectNext(status)
                .verifyComplete();

        verify(reactiveRepository).findByName(DomainConstants.DEFAULT_PENDING_STATUS);
        verify(mapper).toDomain(statusEntity);
    }

    @Test
    void findByNameReturnsEmptyWhenNotFound() {
        when(reactiveRepository.findByName("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByName("UNKNOWN"))
                .verifyComplete();

        verify(reactiveRepository).findByName("UNKNOWN");
        verify(mapper, never()).toDomain(any());
    }
}
