package co.com.pragma.crediya.sqs.sender.exceptions;

import co.com.pragma.crediya.sqs.sender.constants.ErrorMessages;

public class LoanSerializationException extends RuntimeException {

    public LoanSerializationException() {
        super(ErrorMessages.FAILED_TO_SERIALIZE_APPLICATION_RISK_EVALUATION);
    }

}
