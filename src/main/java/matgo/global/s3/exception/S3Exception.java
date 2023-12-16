package matgo.global.s3.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class S3Exception extends CustomException {

    public S3Exception(ErrorCode errorCode) {
        super(errorCode);
    }
}
