package co.com.pragma.crediya.usecase.loan;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.exceptions.ApplicationValueOutOfBoundsException;
import co.com.pragma.crediya.model.loan.exceptions.TypeNotFoundException;
import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.loan.gateways.StatusRepository;
import co.com.pragma.crediya.model.loan.gateways.TypeRepository;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.transaction.gateways.TransactionalPort;
import co.com.pragma.crediya.model.user.User;
import co.com.pragma.crediya.model.user.gateways.UserPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationUseCaseTest {

    @Mock
    private TypeRepository typeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserPort userPort;
    @Mock
    private LoggerPort logger;

    @Mock
    private TransactionalPort transactionalPort;

    @InjectMocks
    private ApplicationUseCase useCase;

    private User loggedUser;

    private Application application;

    private Type type;

    private Status status;

    @BeforeEach
    void setUp() {
        when(transactionalPort.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        loggedUser = new User("1234567890", "jhondoe@example.com");

        type = new Type(UUID.randomUUID(), DomainConstants.MICROCREDIT, BigDecimal.valueOf(300000), BigDecimal.valueOf(50000000), BigDecimal.valueOf(25.00), true);

        status = new Status(UUID.randomUUID(), DomainConstants.DEFAULT_PENDING_STATUS, "Application received, under evaluation");

        application = new Application(UUID.randomUUID(), BigDecimal.valueOf(4000000), 12, loggedUser.identificationNumber(), loggedUser.email(), type, status);
    }

    @Test
    void saveApplicationSuccessfully() {
        when(userPort.getUserByIdentificationNumber(loggedUser.identificationNumber())).thenReturn(Mono.just(loggedUser));

        when(typeRepository.findByName(DomainConstants.MICROCREDIT)).thenReturn(Mono.just(type));

        when(statusRepository.findByName(DomainConstants.DEFAULT_PENDING_STATUS)).thenReturn(Mono.just(status));

        when(applicationRepository.save(any(Application.class))).thenReturn(Mono.just(application));

        StepVerifier.create(useCase.save(application))
                .assertNext(loanApplication -> {
                    Assertions.assertNotNull(loanApplication.id());
                    Assertions.assertEquals(loanApplication.amount(), application.amount());
                    Assertions.assertEquals(loanApplication.term(), application.term());
                    Assertions.assertEquals(loanApplication.identificationNumber(), application.identificationNumber());
                    Assertions.assertEquals(loanApplication.email(), application.email());

                    Assertions.assertNotNull(loanApplication.type());
                    Assertions.assertEquals(loanApplication.type().id(), application.type().id());
                    Assertions.assertEquals(loanApplication.type().name(), application.type().name());

                    Assertions.assertNotNull(loanApplication.status());
                    Assertions.assertEquals(loanApplication.status().id(), application.status().id());
                    Assertions.assertEquals(loanApplication.status().name(), application.status().name());
                })
                .verifyComplete();

        verify(userPort).getUserByIdentificationNumber(loggedUser.identificationNumber());
        verify(typeRepository).findByName(DomainConstants.MICROCREDIT);
        verify(statusRepository).findByName(DomainConstants.DEFAULT_PENDING_STATUS);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void saveApplicationFailsWhenTypeNotFound() {
        when(userPort.getUserByIdentificationNumber(loggedUser.identificationNumber())).thenReturn(Mono.just(loggedUser));

        when(typeRepository.findByName(DomainConstants.MICROCREDIT)).thenReturn(Mono.empty());

        when(statusRepository.findByName(DomainConstants.DEFAULT_PENDING_STATUS)).thenReturn(Mono.just(status));

        StepVerifier.create(useCase.save(application))
                .expectError(TypeNotFoundException.class)
                .verify();

        verify(userPort).getUserByIdentificationNumber(loggedUser.identificationNumber());
        verify(typeRepository).findByName(DomainConstants.MICROCREDIT);
        verify(statusRepository).findByName(DomainConstants.DEFAULT_PENDING_STATUS);
    }

    @Test
    void saveApplicationFailsWhenAmountOutOfRange() {
        when(userPort.getUserByIdentificationNumber(loggedUser.identificationNumber())).thenReturn(Mono.just(loggedUser));

        Type restrictedType = new Type(
                UUID.randomUUID(),
                DomainConstants.MICROCREDIT,
                BigDecimal.valueOf(10000000),
                BigDecimal.valueOf(20000000),
                BigDecimal.valueOf(25.00),
                true
        );
        when(typeRepository.findByName(DomainConstants.MICROCREDIT)).thenReturn(Mono.just(restrictedType));

        when(statusRepository.findByName(DomainConstants.DEFAULT_PENDING_STATUS)).thenReturn(Mono.just(status));

        StepVerifier.create(useCase.save(application))
                .expectError(ApplicationValueOutOfBoundsException.class)
                .verify();

        verify(userPort).getUserByIdentificationNumber(loggedUser.identificationNumber());
        verify(typeRepository).findByName(DomainConstants.MICROCREDIT);
        verify(statusRepository).findByName(DomainConstants.DEFAULT_PENDING_STATUS);

    }

}
