package co.com.pragma.crediya.usecase.loan;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.constants.ApplicationConstants;
import co.com.pragma.crediya.model.loan.constants.ApplicationErrorMessages;
import co.com.pragma.crediya.model.loan.exceptions.*;
import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.notification.gateways.NotificationPort;
import co.com.pragma.crediya.model.loan.gateways.StatusRepository;
import co.com.pragma.crediya.model.loan.gateways.TypeRepository;
import co.com.pragma.crediya.model.notification.NotificationMessage;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.transaction.gateways.TransactionalPort;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplicationUseCase(TypeRepository typeRepository,
                                 StatusRepository statusRepository,
                                 ApplicationRepository applicationRepository,
                                 NotificationPort notificationPort,
                                 LoggerPort logger,
                                 TransactionalPort transactionalPort) {

    public Mono<Application> save(Application application, Jwt token) {
        String identificationNumber = token.identificationNumber();
        String email = token.subject();

        logger.info("Starting save operation for user with identification number: {}", identificationNumber);

        Status status = new Status(null, DomainConstants.DEFAULT_PENDING_STATUS, null);
        Application applicationWithPendingStatusAndUserEmail = new Application(
                null,
                application.amount(),
                application.term(),
                identificationNumber,
                email,
                application.type(),
                status
        );

        return processAndSaveApplication(applicationWithPendingStatusAndUserEmail);
    }

    public Mono<Application> processAndApproveOrReject(UUID id, String newStatus) {
        logger.info("Starting process to approve or reject loan application with ID: {} and new status: {}", id, newStatus);

        if (!isValidStatusTransition(newStatus)) {
            return Mono.error(new ApplicationCannotBeProcessedException(id, newStatus));
        }

        return findApplicationById(id)
                .flatMap(application -> Mono.zip(
                        findTypeById(application.type().id()),
                        findStatusById(application.status().id()),
                        (type, status) -> buildApplicationWithTypeAndStatus(application, type, status)
                ))
                .flatMap(application -> {
                    if (!isApplicationProcessable(application)) {
                        return Mono.error(new ApplicationCannotBeProcessedException(id));
                    }

                    return findStatusByName(newStatus)
                            .map(status -> buildApplicationWithTypeAndStatus(application, application.type(), status));
                })
                .flatMap(application ->
                        applicationRepository.save(application)
                                .map(storedApplication -> buildApplicationWithTypeAndStatus(storedApplication, application.type(), application.status()))
                                .doOnSuccess(storedApplication -> {
                                    String body = String.format(ApplicationConstants.LOAN_STATUS_UPDATE_BODY_TEMPLATE, storedApplication.status().name().toLowerCase());

                                    NotificationMessage notification = new NotificationMessage(storedApplication.email(), ApplicationConstants.LOAN_STATUS_UPDATE_SUBJECT, body);

                                    notificationPort.sendNotification(notification);
                                })
                )
                .as(transactionalPort::transactional)
                .doOnSuccess(storedApplication -> logger.info("Loan application with ID {} updated successfully.", storedApplication.id()))
                .doOnError(e -> logger.error("Failed to update loan application with ID {}. Reason: {}", id, e.getMessage(), e));
    }

    private Mono<Application> processAndSaveApplication(Application application) {
        Mono<Type> loanTypeMono = findTypeByName(application.type().name());

        Mono<Status> loanStatusMono = findStatusByName(application.status().name());

        return Mono.zip(loanTypeMono, loanStatusMono)
                .flatMap(tuple -> {
                    Type loanType = tuple.getT1();
                    Status loanStatus = tuple.getT2();

                    Application applicationWithTypeAndStatus = buildApplicationWithTypeAndStatus(application, loanType, loanStatus);

                    return validateLoanAmount(applicationWithTypeAndStatus)
                            .flatMap(applicationRepository::save)
                            .map(loan -> buildApplicationWithTypeAndStatus(loan, loanType, loanStatus));
                })
                .as(transactionalPort::transactional)
                .doOnSuccess(storedApplication -> logger.info("Loan application saved successfully with id: {}", storedApplication.id()))
                .doOnError(e -> logger.error("Failed to save loan application for user with identification number: " + application.identificationNumber(), e));
    }

    private Mono<Application> findApplicationById(UUID id) {
        return applicationRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ApplicationNotFoundException())))
                .doOnError(ex -> logger.warn(ApplicationErrorMessages.APPLICATION_NOT_FOUND, ex));
    }

    private Mono<Type> findTypeById(UUID id) {
        return typeRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TypeNotFoundException())))
                .doOnError(ex -> logger.warn(ApplicationErrorMessages.TYPE_NOT_FOUND, ex));
    }

    private Mono<Type> findTypeByName(String name) {
        return typeRepository.findByName(name)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TypeNotFoundException())))
                .doOnError(ex -> logger.warn(ApplicationErrorMessages.TYPE_NOT_FOUND, ex));
    }

    private Mono<Status> findStatusById(UUID id) {
        return statusRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new StatusNotFoundException())))
                .doOnError(ex -> logger.warn(ApplicationErrorMessages.STATUS_NOT_FOUND, ex));
    }

    private Mono<Status> findStatusByName(String name) {
        return statusRepository.findByName(name)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new StatusNotFoundException())))
                .doOnError(ex -> logger.warn(ApplicationErrorMessages.STATUS_NOT_FOUND, ex));
    }

    private Application buildApplicationWithTypeAndStatus(Application application, Type type, Status status) {
        return new Application(
                application.id(),
                application.amount(),
                application.term(),
                application.identificationNumber(),
                application.email(),
                type,
                status
        );
    }

    private Mono<Application> validateLoanAmount(Application application) {
        BigDecimal amount = application.amount();
        BigDecimal minValue = application.type().minimumAmount();
        BigDecimal maxValue = application.type().maximumAmount();

        if (amount.compareTo(minValue) < 0 || amount.compareTo(maxValue) > 0) {
            return Mono.error(new ApplicationValueOutOfBoundsException(minValue, maxValue));
        }

        return Mono.just(application);
    }

    private boolean isValidStatusTransition(String newStatus) {
        return DomainConstants.APPROVED_STATUS.equalsIgnoreCase(newStatus) || DomainConstants.REJECTED_STATUS.equalsIgnoreCase(newStatus);
    }

    private boolean isApplicationProcessable(Application application) {
        String currentStatus = application.status().name().toUpperCase();

        return DomainConstants.DEFAULT_PENDING_STATUS.equals(currentStatus) || DomainConstants.MANUAL_REVIEW_STATUS.equals(currentStatus);
    }

}
