package co.com.pragma.crediya.model.loan.exceptions;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.constants.ApplicationConstants;

import java.util.UUID;

public class ApplicationCannotBeProcessedException extends RuntimeException {

    public ApplicationCannotBeProcessedException(UUID id, String currentStatus) {
        super(String.format(
                ApplicationConstants.APPLICATION_INVALID_STATUS,
                id,
                currentStatus,
                String.join(", ", DomainConstants.APPROVED_STATUS, DomainConstants.REJECTED_STATUS)
        ));
    }

    public ApplicationCannotBeProcessedException(UUID id) {
        super(String.format(
                ApplicationConstants.APPLICATION_CANNOT_BE_PROCESSED,
                id,
                String.join(" ", DomainConstants.DEFAULT_PENDING_STATUS, ApplicationConstants.OR_CONNECTOR, DomainConstants.MANUAL_REVIEW_STATUS)
        ));

    }

}
