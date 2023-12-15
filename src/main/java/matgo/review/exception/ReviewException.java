package matgo.review.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class ReviewException extends CustomException {

    public ReviewException(ErrorCode errorCode) {
        super(errorCode);
    }

}
