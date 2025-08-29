package co.com.pragma.crediya.model.user.exceptions;

import co.com.pragma.crediya.model.user.constants.UserErrorMessages;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super(UserErrorMessages.USER_NOT_FOUND);
    }

}
