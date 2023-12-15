package matgo.comment.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class CommentException extends CustomException {

    public CommentException(ErrorCode errorCode) {
        super(errorCode);
    }

}
