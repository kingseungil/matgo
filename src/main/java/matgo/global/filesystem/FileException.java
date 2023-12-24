package matgo.global.filesystem;

import matgo.global.exception.CustomException;
import matgo.global.exception.ErrorCode;

public class FileException extends CustomException {

    public FileException(ErrorCode errorCode) {
        super(errorCode);
    }
}