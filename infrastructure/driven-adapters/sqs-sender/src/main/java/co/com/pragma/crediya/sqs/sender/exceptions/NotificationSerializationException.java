package co.com.pragma.crediya.sqs.sender.exceptions;

import co.com.pragma.crediya.sqs.sender.constants.ErrorMessages;

public class NotificationSerializationException extends RuntimeException {

    public NotificationSerializationException() {
        super(ErrorMessages.FAILED_TO_SERIALIZE_NOTIFICATION_MESSAGE);
    }

}

