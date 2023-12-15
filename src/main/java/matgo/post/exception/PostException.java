package matgo.post.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class PostException extends CustomException {

    public PostException(ErrorCode errorCode) {
        super(errorCode);
    }

}
