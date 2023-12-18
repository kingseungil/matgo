package matgo.auth.presentation;

import static matgo.auth.presentation.AuthDocument.verifyEmailCodeDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import matgo.auth.dto.request.EmailVerificationRequest;
import matgo.common.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthControllerTest extends BaseControllerTest {


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
          .post("/api/auth/verify");

        // then
        assertThat(response.statusCode()).isEqualTo(200);
    }

}