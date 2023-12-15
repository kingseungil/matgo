package matgo.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import matgo.global.exception.dto.ErrorLogRequest;
import matgo.global.exception.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<String>> handleInvalidDtoField(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = Objects.requireNonNull(error.getCodes())[1];
            String errorMessage = error.getDefaultMessage();
            String invalidValue = Objects.requireNonNull(error.getArguments())[1].toString();
            errors.add(String.format(INVALID_DTO_FIELD_ERROR_MESSAGE_FORMAT, fieldName, errorMessage, invalidValue));
        });

        return ResponseEntity.badRequest().body(errors);
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
