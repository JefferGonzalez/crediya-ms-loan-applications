package co.com.pragma.crediya.usecase.loan;

import co.com.pragma.crediya.model.StatusChange;
import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.common.validation.ValidationOutcome;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.loan.*;
import co.com.pragma.crediya.model.loan.constants.ApplicationConstants;
import co.com.pragma.crediya.model.loan.constants.ApplicationErrorMessages;
import co.com.pragma.crediya.model.loan.exceptions.*;
import co.com.pragma.crediya.model.loan.gateways.*;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.notification.LoanApproval;
import co.com.pragma.crediya.model.notification.NotificationMessage;
import co.com.pragma.crediya.model.notification.gateways.NotificationPort;
import co.com.pragma.crediya.model.notification.gateways.NotificationRendererPort;
import co.com.pragma.crediya.model.transaction.gateways.TransactionalPort;
import co.com.pragma.crediya.usecase.loan.utils.ApplicationCalculatorUtils;
import co.com.pragma.crediya.usecase.loan.validation.ValidationLoanApplicationOrchestrator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ApplicationUseCase(TypeRepository typeRepository,
                                 StatusRepository statusRepository,
                                 ApplicationRepository applicationRepository,
                                 ValidationLoanApplicationOrchestrator validationLoanApplicationOrchestrator,
                                 LoanValidationPort loanValidationPort,
                                 LoanApprovedEventPort loanApprovedEventPort,
                                 NotificationPort notificationPort,
                                 NotificationRendererPort notificationRendererPort,
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

        return processAndSaveApplication(applicationWithPendingStatusAndUserEmail, token.baseSalary());
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
                .flatMap(this::saveApplication)
                .as(transactionalPort::transactional)
                .flatMap(storedApplication -> {
                            if (DomainConstants.APPROVED_STATUS.equalsIgnoreCase(storedApplication.status().name())) {
                                ApprovedApplication approvedApplication = new ApprovedApplication(
                                        storedApplication.id(), storedApplication.amount(), OffsetDateTime.now());

                                return loanApprovedEventPort.sendLoanApprovedEvent(approvedApplication)
                                        .then(sendStatusNotification(storedApplication, storedApplication.status().name()))
                                        .thenReturn(storedApplication);
                            }

                            return sendStatusNotification(storedApplication, storedApplication.status().name())
                                    .thenReturn(storedApplication);
                        }
                )
                .doOnSuccess(storedApplication -> logger.info("Loan application with ID {} updated successfully.", storedApplication.id()))
                .doOnError(e -> logger.error("Failed to update loan application with ID {}. Reason: {}", id, e.getMessage(), e));
    }

    public Mono<Void> processApplicationStatusUpdate(LoanValidation result) {
        logger.info("Starting application status update for application ID: {} with new status: {}", result.id(), result.status());

        return findStatusByName(result.status())
                .flatMap(status ->
                        findApplicationById(result.id())
                                .flatMap(application ->
                                        findTypeById(application.type().id())
                                                .map(type -> buildApplicationWithTypeAndStatus(application, type, status))
                                )
                )
                .flatMap(this::saveApplication)
                .as(transactionalPort::transactional)
                .flatMap(storedApplication -> {
                    logger.info("Application with ID {} updated to status {}", storedApplication.id(), result.status());

                    if (DomainConstants.APPROVED_STATUS.equalsIgnoreCase(result.status())) {
                        ApprovedApplication approvedApplication = new ApprovedApplication(
                                storedApplication.id(), storedApplication.amount(), OffsetDateTime.now());

                        return loanApprovedEventPort.sendLoanApprovedEvent(approvedApplication)
                                .then(sendApprovalNotification(storedApplication, result));
                    }

                    return sendStatusNotification(storedApplication, result.status());
                })
                .doOnError(e -> logger.error("Failed to update application status for ID {}. Reason: {}", result.id(), e.getMessage(), e));
    }

    private Mono<Application> processAndSaveApplication(Application application, BigDecimal baseSalary) {
        Mono<Type> loanTypeMono = findTypeByName(application.type().name());

        Mono<Status> loanStatusMono = findStatusByName(application.status().name());

        return Mono.zip(loanTypeMono, loanStatusMono)
                .flatMap(tuple -> {
                    Type loanType = tuple.getT1();
                    Status loanStatus = tuple.getT2();

                    Application applicationWithTypeAndStatus = buildApplicationWithTypeAndStatus(application, loanType, loanStatus);

                    return validateApplicationBusinessRules(applicationWithTypeAndStatus)
                            .flatMap(this::saveApplication);
                })
                .as(transactionalPort::transactional)
                .flatMap(storedApplication ->
                        afterApplicationSaved(storedApplication, baseSalary)
                                .thenReturn(storedApplication)
                )
                .doOnSuccess(storedApplication -> logger.info("Loan application saved successfully with id: {}", storedApplication.id()))
                .doOnError(e -> logger.error("Failed to save loan application for user with identification number: " + application.identificationNumber(), e));
    }

    private Mono<Void> sendStatusNotification(Application application, String status) {
        StatusChange statusChange = new StatusChange(status.toLowerCase(), application.type().name(), application.amount());

        return notificationRendererPort.processStatusChangeTemplate(statusChange)
                .flatMap(html -> sendEmailNotification(application.email(), html));
    }

    private Mono<Void> sendApprovalNotification(Application application, LoanValidation result) {
        LoanApproval approval = new LoanApproval(
                application.type().name(),
                application.amount(),
                application.term(),
                ApplicationCalculatorUtils.annualToMonthlyRate(application.type().interestRate()),
                result.paymentDetail().getFirst().installment(),
                result.paymentDetail()
        );

        return notificationRendererPort.processLoanApprovalTemplate(approval)
                .flatMap(html -> sendEmailNotification(application.email(), html));
    }

    private Mono<Void> sendEmailNotification(String email, String body) {
        NotificationMessage notification = new NotificationMessage(
                email,
                ApplicationConstants.LOAN_STATUS_UPDATE_SUBJECT,
                body
        );
        return notificationPort.sendNotification(notification);
    }

    private Mono<Application> saveApplication(Application application) {
        return applicationRepository.save(application)
                .map(storedApplication -> buildApplicationWithTypeAndStatus(storedApplication, application.type(), application.status()));
    }

    private Mono<Void> afterApplicationSaved(Application application, BigDecimal baseSalary) {
        if (Boolean.FALSE.equals(application.type().automaticValidation())) return Mono.empty();

        return applicationRepository.findActiveLoansByIdentificationNumber(application.identificationNumber())
                .collectList()
                .map(activeApplications ->
                        new ApplicationRiskEvaluation(
                                application.id(),
                                application.type().name(),
                                application.amount(),
                                application.term(),
                                ApplicationCalculatorUtils.annualToMonthlyRate(application.type().interestRate()),
                                baseSalary,
                                activeApplications
                        )
                )
                .flatMap(loanValidationPort::validateLoanAutomatically);
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

    private Mono<Application> validateApplicationBusinessRules(Application application) {
        return validationLoanApplicationOrchestrator.validateApplicationBusinessRules(application)
                .flatMap(outcomes -> {
                    List<ValidationOutcome> errors = outcomes.stream()
                            .filter(outcome -> !outcome.isValid())
                            .toList();

                    if (!errors.isEmpty()) {
                        return Mono.error(new ApplicationBusinessValidationException(errors));
                    }

                    return Mono.just(application);
                });
    }

    private boolean isValidStatusTransition(String newStatus) {
        return DomainConstants.APPROVED_STATUS.equalsIgnoreCase(newStatus) || DomainConstants.REJECTED_STATUS.equalsIgnoreCase(newStatus);
    }

    private boolean isApplicationProcessable(Application application) {
        String currentStatus = application.status().name().toUpperCase();

        return DomainConstants.DEFAULT_PENDING_STATUS.equals(currentStatus) || DomainConstants.MANUAL_REVIEW_STATUS.equals(currentStatus);
    }

}
