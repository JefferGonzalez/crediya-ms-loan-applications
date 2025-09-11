package co.com.pragma.crediya.usecase.loan;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.constants.ApplicationConstants;
import co.com.pragma.crediya.model.loan.exceptions.*;
import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.loan.gateways.StatusRepository;
import co.com.pragma.crediya.model.loan.gateways.TypeRepository;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.notification.NotificationMessage;
import co.com.pragma.crediya.model.notification.gateways.NotificationPort;
import co.com.pragma.crediya.model.transaction.gateways.TransactionalPort;
import co.com.pragma.crediya.model.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationUseCaseTest {

    @Mock
    private TypeRepository typeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    NotificationPort notificationPort;

    @Mock
    private LoggerPort logger;

    @Mock
    private TransactionalPort transactionalPort;

    @Mock
    private Jwt mockJwt;

    private ApplicationUseCase useCase;

    private Application application;

    private Type type;

    private Status status;

    @BeforeEach
    void setUp() {
        useCase = new ApplicationUseCase(typeRepository, statusRepository, applicationRepository, notificationPort, logger, transactionalPort);

        User loggedUser = new User("1234567890", "jhondoe@example.com", BigDecimal.valueOf(1200000));

        type = new Type(UUID.randomUUID(), DomainConstants.MICROCREDIT, BigDecimal.valueOf(300000), BigDecimal.valueOf(50000000), BigDecimal.valueOf(25.00), true);

        status = new Status(UUID.randomUUID(), DomainConstants.DEFAULT_PENDING_STATUS, "Application received, under evaluation");

        application = new Application(UUID.randomUUID(), BigDecimal.valueOf(4000000), 12, loggedUser.identificationNumber(), loggedUser.email(), type, status);

        lenient().when(transactionalPort.transactional(any(Mono.class))).then(returnsFirstArg());

        lenient().when(mockJwt.subject()).thenReturn(loggedUser.email());

        lenient().when(mockJwt.identificationNumber()).thenReturn(loggedUser.identificationNumber());
    }

    @Test
    void saveApplicationSuccessfully() {
        when(typeRepository.findByName(DomainConstants.MICROCREDIT)).thenReturn(Mono.just(type));

        when(statusRepository.findByName(DomainConstants.DEFAULT_PENDING_STATUS)).thenReturn(Mono.just(status));

        when(applicationRepository.save(any(Application.class))).thenReturn(Mono.just(application));

        StepVerifier.create(useCase.save(application, mockJwt))
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

        verify(typeRepository).findByName(DomainConstants.MICROCREDIT);

        verify(statusRepository).findByName(DomainConstants.DEFAULT_PENDING_STATUS);

        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void saveApplicationFailsWhenTypeNotFound() {
        when(typeRepository.findByName(DomainConstants.MICROCREDIT)).thenReturn(Mono.empty());

        when(statusRepository.findByName(DomainConstants.DEFAULT_PENDING_STATUS)).thenReturn(Mono.just(status));

        StepVerifier.create(useCase.save(application, mockJwt))
                .expectError(TypeNotFoundException.class)
                .verify();

        verify(typeRepository).findByName(DomainConstants.MICROCREDIT);

        verify(statusRepository).findByName(DomainConstants.DEFAULT_PENDING_STATUS);
    }

    @Test
    void saveApplicationFailsWhenStatusNotFound() {
        when(typeRepository.findByName(DomainConstants.MICROCREDIT)).thenReturn(Mono.just(type));

        when(statusRepository.findByName(DomainConstants.DEFAULT_PENDING_STATUS)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.save(application, mockJwt))
                .expectError(StatusNotFoundException.class)
                .verify();
    }

    @Test
    void saveApplicationFailsWhenAmountOutOfRange() {
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

        StepVerifier.create(useCase.save(application, mockJwt))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(ApplicationValueOutOfBoundsException.class)
                        .hasMessage("amount most be between 10.000.000,00 and 20.000.000,00"))
                .verify();

        verify(typeRepository).findByName(DomainConstants.MICROCREDIT);

        verify(statusRepository).findByName(DomainConstants.DEFAULT_PENDING_STATUS);
    }

    @Test
    void processAndApproveApplicationSuccessfully() {
        UUID appId = application.id();
        String newStatus = DomainConstants.APPROVED_STATUS;

        when(applicationRepository.findById(appId)).thenReturn(Mono.just(application));

        when(typeRepository.findById(application.type().id())).thenReturn(Mono.just(type));

        when(statusRepository.findById(application.status().id())).thenReturn(Mono.just(status));

        Status approved = new Status(UUID.randomUUID(), newStatus, "Application approved");

        when(statusRepository.findByName(newStatus)).thenReturn(Mono.just(approved));

        when(applicationRepository.save(any(Application.class))).thenReturn(Mono.just(application));

        doNothing().when(notificationPort).sendNotification(any(NotificationMessage.class));

        StepVerifier.create(useCase.processAndApproveOrReject(appId, newStatus))
                .assertNext(updated -> {
                    assertThat(updated.id()).isEqualTo(application.id());
                    assertThat(updated.status().name()).isEqualToIgnoringCase(newStatus);
                    assertThat(updated.type().id()).isEqualTo(application.type().id());
                    assertThat(updated.email()).isEqualTo(application.email());
                })
                .verifyComplete();

        verify(applicationRepository).save(any(Application.class));

        verify(notificationPort).sendNotification(any(NotificationMessage.class));
    }

    @Test
    void processAndRejectApplicationSuccessfully() {
        UUID appId = application.id();
        String newStatus = DomainConstants.REJECTED_STATUS;

        when(applicationRepository.findById(appId)).thenReturn(Mono.just(application));

        when(typeRepository.findById(application.type().id())).thenReturn(Mono.just(type));

        when(statusRepository.findById(application.status().id())).thenReturn(Mono.just(status));

        Status rejected = new Status(UUID.randomUUID(), newStatus, "Application rejected");

        when(statusRepository.findByName(newStatus)).thenReturn(Mono.just(rejected));

        when(applicationRepository.save(any(Application.class))).thenReturn(Mono.just(application));

        doNothing().when(notificationPort).sendNotification(any(NotificationMessage.class));

        StepVerifier.create(useCase.processAndApproveOrReject(appId, newStatus))
                .assertNext(updated -> {
                    assertThat(updated.id()).isEqualTo(application.id());
                    assertThat(updated.status().name()).isEqualToIgnoringCase(newStatus);
                })
                .verifyComplete();

        verify(applicationRepository).save(any(Application.class));

        verify(notificationPort).sendNotification(any(NotificationMessage.class));
    }

    @Test
    void processFailsWhenInvalidStatusTransition() {
        UUID appId = application.id();

        String permittedStatuses = String.join(", ", DomainConstants.APPROVED_STATUS, DomainConstants.REJECTED_STATUS);
        String expectedMessage = String.format(ApplicationConstants.APPLICATION_INVALID_STATUS, appId, DomainConstants.DEFAULT_PENDING_STATUS, permittedStatuses);

        StepVerifier.create(useCase.processAndApproveOrReject(appId, DomainConstants.DEFAULT_PENDING_STATUS))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(ApplicationCannotBeProcessedException.class)
                        .hasMessage(expectedMessage))
                .verify();

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void processFailsWhenApplicationIsNotProcessable() {
        Status approved = new Status(UUID.randomUUID(), DomainConstants.APPROVED_STATUS, "Application approved");
        Application approvedApplication = new Application(
                UUID.randomUUID(),
                application.amount(),
                application.term(),
                application.identificationNumber(),
                application.email(),
                application.type(),
                approved
        );

        String expectedStatuses = String.join(" ", DomainConstants.DEFAULT_PENDING_STATUS, ApplicationConstants.OR_CONNECTOR, DomainConstants.MANUAL_REVIEW_STATUS);
        String expectedMessage = String.format(ApplicationConstants.APPLICATION_CANNOT_BE_PROCESSED, approvedApplication.id(), expectedStatuses);

        when(applicationRepository.findById(approvedApplication.id())).thenReturn(Mono.just(approvedApplication));

        when(typeRepository.findById(approvedApplication.type().id())).thenReturn(Mono.just(approvedApplication.type()));

        when(statusRepository.findById(approvedApplication.status().id())).thenReturn(Mono.just(approved));

        StepVerifier.create(useCase.processAndApproveOrReject(approvedApplication.id(), DomainConstants.APPROVED_STATUS))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(ApplicationCannotBeProcessedException.class)
                        .hasMessage(expectedMessage))
                .verify();

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void processFailsWhenApplicationNotFound() {
        UUID appId = UUID.randomUUID();

        when(applicationRepository.findById(appId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.processAndApproveOrReject(appId, DomainConstants.APPROVED_STATUS))
                .expectError(ApplicationNotFoundException.class)
                .verify();

        verify(applicationRepository).findById(appId);

        verify(applicationRepository, never()).save(any(Application.class));
    }

}
