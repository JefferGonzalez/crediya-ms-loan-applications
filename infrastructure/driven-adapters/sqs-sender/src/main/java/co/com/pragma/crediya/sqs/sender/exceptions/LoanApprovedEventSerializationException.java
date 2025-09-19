package co.com.pragma.crediya.sqs.sender.exceptions;

import co.com.pragma.crediya.sqs.sender.constants.ErrorMessages;

public class LoanApprovedEventSerializationException extends RuntimeException {

    public LoanApprovedEventSerializationException() {
        super(ErrorMessages.FAILED_TO_SERIALIZE_LOAN_APPROVED_EVENT);
    }

}