package matgo.auth.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class AuthException extends CustomException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
    
}
