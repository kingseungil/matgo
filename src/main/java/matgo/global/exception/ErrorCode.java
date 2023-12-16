package matgo.global.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // Auth
    EXPIRED_VERIFICATION_CODE(HttpStatus.UNAUTHORIZED, "인증 코드가 만료되었습니다."),
    UNMATCHED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    ALREADY_VERIFIED_EMAIL(HttpStatus.BAD_REQUEST, "이미 인증된 이메일입니다."),

    // S3
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    // Member
    ALREADY_EXISTED_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    ALREADY_EXISTED_NICKNAME(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임입니다."),
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."),

    // Region
    NOT_FOUND_REGION(HttpStatus.BAD_REQUEST, "존재하지 않는 지역입니다."),

    // Mail
    MAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "메일 전송에 실패했습니다.");


    private final HttpStatus status;
    private final String message;
}
