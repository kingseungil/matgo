package matgo.member.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class MemberException extends CustomException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }

}
