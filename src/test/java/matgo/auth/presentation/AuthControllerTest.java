package matgo.auth.presentation;

import static matgo.auth.presentation.AuthDocument.loginDocument;
import static matgo.auth.presentation.AuthDocument.logoutDocument;
import static matgo.auth.presentation.AuthDocument.sendTemporayPasswordDocument;
import static matgo.auth.presentation.AuthDocument.verifyEmailCodeDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import matgo.auth.dto.request.EmailVerificationRequest;
import matgo.auth.dto.request.LoginRequest;
import matgo.common.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthControllerTest extends BaseControllerTest {

    public static ExtractableResponse<Response> loginMember(LoginRequest loginRequest) {
        return customGiven()
          .contentType(ContentType.JSON)
          .body(loginRequest)
          .post("/api/auth/login")
          .then()
          .extract();
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
        assertThat(response.statusCode()).isEqualTo(204);
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
        // given,when
        Response response = customGivenWithDocs(logoutDocument())
          .header("Authorization", "Bearer " + accessToken)
          .delete("/api/auth/logout");

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    @DisplayName("임시 비밀번호 발급")
    void sendTemporaryPassword() {
        // given,when
        Response response = customGivenWithDocs(sendTemporayPasswordDocument())
          .contentType(ContentType.JSON)
          .body(sendTemporaryPasswordRequest)
          .post("/api/auth/send-temporary-password");

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }
}