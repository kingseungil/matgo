package matgo.auth.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class MailException extends CustomException {

    public MailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
