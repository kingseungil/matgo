package matgo.restaurant.feignclient.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class FeignClientException extends CustomException {

    public FeignClientException(ErrorCode errorCode) {
        super(errorCode);
    }
}
