package matgo.global.util;

public class DtoValidator {

    public static final String EMPTY_MESSAGE = "비어있는 항목을 입력해주세요.";
    public static final String EMAIL_MESSAGE = "올바른 이메일 형식이 아닙니다.";
    public static final String PW_MESSAGE = "비밀번호는 특수문자와 숫자를 포함하여 8자 이상 20자 이내로 작성 가능합니다.";
    public static final String NICKNAME_MESSAGE = "이름은 특수문자를 제외하고 공백없이 10자 이내로 작성 가능합니다.";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssxxx";
    public static final String PW_FORMAT = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W).{8,20}$";
    public static final String NAMING_FORMAT = "^[a-zA-Z0-9\\p{IsHangul}]{1,10}$";
}
