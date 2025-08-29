package co.com.pragma.crediya.model.user.exceptions;

import co.com.pragma.crediya.model.user.constants.UserErrorMessages;

public class UserDataInconsistencyException  extends RuntimeException {

    public UserDataInconsistencyException () {
        super(UserErrorMessages.USER_DATA_INCONSISTENCY);
    }

}
