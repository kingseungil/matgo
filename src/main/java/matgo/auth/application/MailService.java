package matgo.auth.application;

import matgo.auth.dto.request.EmailVerificationRequest;

public interface MailService {

    // 인증번호 발송
    String sendVerificationCode(String email);

    //

    // 인증번호 확인
    void verifyCode(EmailVerificationRequest request);

    // 임시 비밀번호 발송
    void sendTemporaryPassword(String email, String password);

}
