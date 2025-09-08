package co.com.pragma.crediya.usecase.loan;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.constants.ApplicationErrorMessages;
import co.com.pragma.crediya.model.loan.exceptions.ApplicationValueOutOfBoundsException;
import co.com.pragma.crediya.model.loan.exceptions.StatusNotFoundException;
import co.com.pragma.crediya.model.loan.exceptions.TypeNotFoundException;
import co.com.pragma.crediya.model.loan.gateways.ApplicationRepository;
import co.com.pragma.crediya.model.loan.gateways.StatusRepository;
import co.com.pragma.crediya.model.loan.gateways.TypeRepository;
import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import co.com.pragma.crediya.model.transaction.gateways.TransactionalPort;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public record ApplicationUseCase(TypeRepository typeRepository,
                                 StatusRepository statusRepository,
                                 ApplicationRepository applicationRepository,
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

    private Mono<Application> processAndSaveApplication(Application application) {
        Mono<Type> loanTypeMono = typeRepository.findByName(application.type().name())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new TypeNotFoundException())))
                .doOnError(ex -> logger.warn(ApplicationErrorMessages.TYPE_NOT_FOUND, ex));

        Mono<Status> loanStatusMono = statusRepository.findByName(application.status().name())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new StatusNotFoundException())))
                .doOnError(ex -> logger.warn(ApplicationErrorMessages.STATUS_NOT_FOUND, ex));

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

}
