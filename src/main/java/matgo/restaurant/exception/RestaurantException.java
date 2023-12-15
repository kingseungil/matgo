package matgo.restaurant.exception;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class RestaurantException extends CustomException {

    public RestaurantException(ErrorCode errorCode) {
        super(errorCode);
    }

}
