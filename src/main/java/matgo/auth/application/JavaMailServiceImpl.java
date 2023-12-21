package matgo.auth.application;

import static matgo.global.exception.ErrorCode.ALREADY_VERIFIED_EMAIL;
import static matgo.global.exception.ErrorCode.EXPIRED_VERIFICATION_CODE;
import static matgo.global.exception.ErrorCode.MAIL_SEND_ERROR;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.UNMATCHED_VERIFICATION_CODE;

import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.auth.dto.request.EmailVerificationRequest;
import matgo.auth.exception.AuthException;
import matgo.auth.exception.MailException;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JavaMailServiceImpl implements MailService {

    private static final String EMAIL_SUBJECT = "Matgo 인증 코드";
    private final MemberRepository memberRepository;
    private final JavaMailSender javaMailSender;

    @Override
    public String sendVerificationCode(String email) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        String verificationCode = generateVerificationCode();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject(EMAIL_SUBJECT);
            helper.setText("인증 코드 : " + verificationCode);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new MailException(MAIL_SEND_ERROR);
        }
        log.info("Sent a verification code: {} to {}", verificationCode, email);

        return verificationCode;
    }

    private String generateVerificationCode() {
        // 이메일 인증 코드 (6자리)
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }


    @Override
    @Transactional
    public void verifyCode(EmailVerificationRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                                        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        validateCode(member, request);
        member.verifyEmail();
        log.info("Verified email: {}", member.getEmail());
    }

    private void validateCode(Member member, EmailVerificationRequest request) {
        // 기간 확인
        if (member.getEmailVerification().isExpired()) {
            throw new AuthException(EXPIRED_VERIFICATION_CODE);
        }
        // 코드 일치하는지 확인
        if (!member.getEmailVerification().getVerificationCode().equals(request.code())) {
            throw new AuthException(UNMATCHED_VERIFICATION_CODE);
        }
        // 이미 인증된 경우
        if (member.isVerified()) {
            throw new AuthException(ALREADY_VERIFIED_EMAIL);
        }
    }

    @Override
    public void sendTemporaryPassword(String email, String password) {

    }
}
