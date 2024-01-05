package matgo.auth.application;

import java.util.concurrent.CompletableFuture;
import matgo.auth.dto.request.EmailVerificationRequest;
import org.springframework.scheduling.annotation.Async;

public interface MailService {

    // 인증번호 발송
    @Async("mailSenderExecutor")
    CompletableFuture<String> sendVerificationCode(String email);

    // 인증번호 확인
    void verifyCode(EmailVerificationRequest request);

    // 임시 비밀번호 발송
    @Async("mailSenderExecutor")
    void sendTemporaryPassword(String email, String password);

}
