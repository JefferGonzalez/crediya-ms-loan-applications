package co.com.pragma.crediya.usecase.loan;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.jwt.gateways.JwtProviderPort;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.Status;
import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.constants.ApplicationConstants;
import co.com.pragma.crediya.model.loan.constants.ApplicationErrorMessages;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;
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
import java.util.StringJoiner;

public record ApplicationUseCase(TypeRepository typeRepository,
                                 StatusRepository statusRepository,
                                 ApplicationRepository applicationRepository,
                                 JwtProviderPort jwtProviderPort,
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

        return execute(applicationWithPendingStatusAndUserEmail);
    }

    private Mono<Application> execute(Application application) {
        Mono<Type> loanTypeMono = typeRepository.findByName(application.type().name())
                .switchIfEmpty(Mono.defer(() -> {
                    TypeNotFoundException ex = new TypeNotFoundException();
                    logger.warn(ApplicationErrorMessages.TYPE_NOT_FOUND, ex);

                    return Mono.error(ex);
                }));

        Mono<Status> loanStatusMono = statusRepository.findByName(application.status().name())
                .switchIfEmpty(Mono.defer(() -> {
                    StatusNotFoundException ex = new StatusNotFoundException();
                    logger.warn(ApplicationErrorMessages.STATUS_NOT_FOUND, ex);

                    return Mono.error(ex);
                }));

        return Mono.zip(loanTypeMono, loanStatusMono)
                .flatMap(tuple -> {
                    Type loanType = tuple.getT1();
                    Status loanStatus = tuple.getT2();

                    ApplicationValueOutOfBoundsException ex = validateLoanAmount(application, loanType);

                    if (ex != null) {
                        return Mono.error(ex);
                    }

                    Application applicationToSave = new Application(null, application.amount(), application.term(), application.identificationNumber(), application.email(), loanType, loanStatus);

                    return applicationRepository.save(applicationToSave)
                            .map(loan -> new Application(loan.id(), loan.amount(), loan.term(), loan.identificationNumber(), loan.email(), loanType, loanStatus));
                })
                .as(transactionalPort::transactional)
                .doOnSuccess(storedApplication -> logger.info("Loan application saved successfully with id: {}", storedApplication.id()))
                .doOnError(e -> logger.error("Failed to save loan application for user with identification number: " + application.identificationNumber(), e));
    }

    private ApplicationValueOutOfBoundsException validateLoanAmount(Application application, Type type) {
        BigDecimal amount = application.amount();
        BigDecimal minValue = type.minimumAmount();
        BigDecimal maxValue = type.maximumAmount();

        if (amount.compareTo(minValue) < 0 || amount.compareTo(maxValue) > 0) {
            return new ApplicationValueOutOfBoundsException(buildLoanAmountRangeMessage(minValue, maxValue));
        }

        return null;
    }

    private String buildLoanAmountRangeMessage(BigDecimal minValue, BigDecimal maxValue) {
        String minValueFormatted = ApplicationConstants.DECIMAL_FORMAT.format(minValue);
        String maxValueFormatted = ApplicationConstants.DECIMAL_FORMAT.format(maxValue);

        StringJoiner message = new StringJoiner(" ");

        message.add(ApplicationFieldNames.AMOUNT);
        message.add(ApplicationConstants.MOST_BE_BETWEEN);
        message.add(minValueFormatted);
        message.add(ApplicationConstants.AND_CONNECTOR);
        message.add(maxValueFormatted);

        return message.toString();
    }

}
