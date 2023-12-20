package matgo.auth.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class TokenException extends CustomException {

    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
