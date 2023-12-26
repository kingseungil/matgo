package matgo.global.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    NOT_SUPPORTED_USER_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 유저 타입입니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.UNAUTHORIZED, "인증 코드가 만료되었습니다."),
    UNMATCHED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    ALREADY_VERIFIED_EMAIL(HttpStatus.BAD_REQUEST, "이미 인증된 이메일입니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    NOT_ACTIVATED_USER(HttpStatus.UNAUTHORIZED, "활성화되지 않은 회원입니다."),

    // TOKEN
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 엑세스토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 엑세스토큰입니다."),
    EMPTY_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "엑세스토큰이 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 리프레시토큰입니다. 다시 로그인해주세요."),
    EMPTY_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시토큰이 존재하지 않습니다."),

    // S3
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

    // File
    NOT_FOUND_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "이미지 확장자를 찾을 수 없습니다."),
    NOT_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "이미지 확장자가 아닙니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 초과되었습니다."),

    // Member
    ALREADY_EXISTED_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    ALREADY_EXISTED_NICKNAME(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임입니다."),
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."),

    // Region
    NOT_FOUND_REGION(HttpStatus.BAD_REQUEST, "존재하지 않는 지역입니다."),

    // Mail
    MAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "메일 전송에 실패했습니다."),

    // OpenFeign
    CANT_PARSE_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "응답을 파싱할 수 없습니다."),

    // Restaurant
    UPDATABLE_RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "업데이트 가능한 식당이 존재하지 않습니다.");


    private final HttpStatus status;
    private final String message;
}
