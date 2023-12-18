package matgo.auth.application;

import static matgo.global.exception.ErrorCode.ALREADY_VERIFIED_EMAIL;
import static matgo.global.exception.ErrorCode.EXPIRED_VERIFICATION_CODE;
import static matgo.global.exception.ErrorCode.UNMATCHED_VERIFICATION_CODE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;
import matgo.auth.domain.entity.EmailVerification;
import matgo.auth.dto.request.EmailVerificationRequest;
import matgo.auth.exception.AuthException;
import matgo.common.BaseServiceTest;
import matgo.member.domain.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

class JavaMailServieImplTest extends BaseServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private MimeMessage mimeMessage;
    @InjectMocks
    private JavaMailServieImpl javaMailService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                       .email("test@naver.com")
                       .isActive(false)
                       .build();

    }

    @Test
    @DisplayName("sendVerificationCode 메서드 성공")
    void sendVerificationCode() {
        // given
        doReturn(Optional.of(member)).when(memberRepository).findByEmail(anyString());
        doReturn(mimeMessage).when(javaMailSender).createMimeMessage();
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        // when
        String verificationCode = javaMailService.sendVerificationCode("test@naver.com");

        // then
        verify(javaMailSender).send(any(MimeMessage.class));
        verify(emailVerificationRepository).save(any(EmailVerification.class));
        assertNotNull(verificationCode);
    }


    @Nested
    @DisplayName("verifyCode 메서드는")
    class verifyCode {

        EmailVerification successCase = new EmailVerification("111111", LocalDateTime.now().plusHours(24),
          member);

        EmailVerification expriedCase = new EmailVerification("111111", LocalDateTime.now().minusHours(24),
          member);

        EmailVerification unmatchedCase = new EmailVerification("111111", LocalDateTime.now().plusHours(24),
          member);


        @Test
        @DisplayName("인증에 성공하면 회원의 이메일 인증 여부를 true로 변경한다.")
        void success() {
            // given
            member.setEmailVerification(successCase);
            EmailVerificationRequest emailVerificationRequest = new EmailVerificationRequest("test@naver.com",
              "111111");
            doReturn(Optional.of(member)).when(memberRepository).findByEmail(anyString());

            // when
            javaMailService.verifyCode(emailVerificationRequest);

            // then
            assertTrue(member.isVerified());
        }

        @Nested
        @DisplayName("인증에 실패하면")
        class Fail {

            @Test
            @DisplayName("기간이 만료되면 예외를 던진다.")
            void expired() {
                // given
                member.setEmailVerification(expriedCase);
                EmailVerificationRequest emailVerificationRequest = new EmailVerificationRequest("test@naver.com",
                  "111111");
                doReturn(Optional.of(member)).when(memberRepository).findByEmail(anyString());

                // when & then
                assertThatThrownBy(() -> javaMailService.verifyCode(emailVerificationRequest))
                  .isInstanceOf(AuthException.class)
                  .hasMessageContaining(EXPIRED_VERIFICATION_CODE.getMessage());
            }

            @Test
            @DisplayName("인증코드가 일치하지 않으면 예외를 던진다.")
            void unmatchedCode() {
                // given
                member.setEmailVerification(unmatchedCase);
                EmailVerificationRequest emailVerificationRequest = new EmailVerificationRequest("test@naver.com",
                  "222222");
                doReturn(Optional.of(member)).when(memberRepository).findByEmail(anyString());

                // when & then
                assertThatThrownBy(() -> javaMailService.verifyCode(emailVerificationRequest))
                  .isInstanceOf(AuthException.class)
                  .hasMessageContaining(UNMATCHED_VERIFICATION_CODE.getMessage());
            }

            @Test
            @DisplayName("이미 인증된 회원이면 예외를 던진다.")
            void alreadyVerified() {
                // given
                member.setEmailVerification(successCase);
                EmailVerificationRequest emailVerificationRequest = new EmailVerificationRequest("test@naver.com",
                  "111111");
                member.verifyEmail();
                doReturn(Optional.of(member)).when(memberRepository).findByEmail(anyString());

                // when & then
                assertThatThrownBy(() -> javaMailService.verifyCode(emailVerificationRequest))
                  .isInstanceOf(AuthException.class)
                  .hasMessageContaining(ALREADY_VERIFIED_EMAIL.getMessage());
            }
        }
    }
}