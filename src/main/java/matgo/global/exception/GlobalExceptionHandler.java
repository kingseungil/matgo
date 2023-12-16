package matgo.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import matgo.global.exception.dto.ErrorLogRequest;
import matgo.global.exception.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String INVALID_DTO_FIELD_ERROR_MESSAGE_FORMAT = "%s 필드는 %s (전달된 값: %s)";

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
    }

    // Validation 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDtoField(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getFieldErrors().get(0);
        String errorMessage = String.format(
          INVALID_DTO_FIELD_ERROR_MESSAGE_FORMAT,
          fieldError.getField(),
          fieldError.getDefaultMessage(),
          fieldError.getRejectedValue()
        );

        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_REQUEST, errorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 기타 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
        ErrorLogRequest errorLogRequest = new ErrorLogRequest(request, e);
        log.error(errorLogRequest.getLogMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "예상하지 못한 서버 에러가 발생했습니다.");
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}