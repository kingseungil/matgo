package matgo.auth.presentation;

import static matgo.auth.presentation.AuthDocument.loginDocument;
import static matgo.auth.presentation.AuthDocument.logoutDocument;
import static matgo.auth.presentation.AuthDocument.verifyEmailCodeDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import matgo.auth.dto.request.EmailVerificationRequest;
import matgo.common.BaseControllerTest;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.domain.repository.RegionRepository;
import matgo.member.domain.type.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthControllerTest extends BaseControllerTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static ExtractableResponse<Response> loginMember() {
        return customGiven()
          .contentType(ContentType.JSON)
          .body(loginRequest)
          .post("/api/auth/login")
          .then()
          .extract();
    }

    @BeforeEach
    void setUp() {
        Region region = regionRepository.save(new Region("효자동"));
        String password = passwordEncoder.encode("1!asdasd");
        memberRepository.save(Member.builder()
                                    .email("test@naver.com")
                                    .nickname("testnick")
                                    .password(password)
                                    .profileImage(
                                      "https://matgo-bucket.s3.ap-northeast-2.amazonaws.com/matgo/member/154064a0-2403-4fb8-9875-048369326ceb")
                                    .role(UserRole.ROLE_USER)
                                    .region(region)
                                    .isActive(true)
                                    .build());

    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        regionRepository.deleteAll();
    }

    @Test
    @DisplayName("이메일 인증")
    void verifyEmail() {
        // given
        doNothing().when(mailService).verifyCode(any(EmailVerificationRequest.class));

        EmailVerificationRequest request = new EmailVerificationRequest("test@naver.com", "123456");

        // when
        Response response = customGivenWithDocs(verifyEmailCodeDocument())
          .contentType(ContentType.JSON)
          .body(request)
          .post("/api/auth/verify-emailcode");

        // then
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("로그인")
    void login() {
        // given,when
        Response response = customGivenWithDocs(loginDocument())
          .contentType(ContentType.JSON)
          .body(loginRequest)
          .post("/api/auth/login");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(response.jsonPath().getString("accessToken")).isNotNull();
        });
    }

    @Test
    @DisplayName("로그아웃")
    void logout() {
        // given
        String accessToken = loginMember().jsonPath().getString("accessToken");

        // when
        Response response = customGivenWithDocs(logoutDocument())
          .header("Authorization", "Bearer " + accessToken)
          .delete("/api/auth/logout");

        System.out.println("response = " + response.asString());

        // then
        assertThat(response.statusCode()).isEqualTo(200);
    }
}