package matgo.global.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import matgo.global.exception.ErrorCode;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private ErrorCode errorCode;
    private String errorMessage;
}
