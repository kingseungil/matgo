package matgo.member.presentation;

import static matgo.member.presentation.MemberDocument.deleteMemberDocument;
import static matgo.member.presentation.MemberDocument.getMemberDetailDocument;
import static matgo.member.presentation.MemberDocument.registerMemberDocument;
import static matgo.member.presentation.MemberDocument.resetPasswordDocument;
import static matgo.member.presentation.MemberDocument.updateMemberDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import matgo.common.BaseControllerTest;
import matgo.global.type.S3Directory;
import matgo.member.dto.request.ResetPasswordRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class MemberControllerTest extends BaseControllerTest {


    @Test
    @DisplayName("[성공]회원가입")
    void registerMember_success() {
        // given
        MultiPartSpecBuilder request = new MultiPartSpecBuilder(signUpRequest);
        request.charset("UTF-8");
        request.controlName("signUpRequest");
        request.mimeType("application/json");
        doReturn("mocked_url").when(s3Service)
                              .uploadAndGetImageURL(any(MultipartFile.class), eq(S3Directory.MEMBER));

        // when
        Response response = customGivenWithDocs(registerMemberDocument())
          .contentType("multipart/form-data;charset=UTF-8")
          .multiPart(request.build())
          .multiPart("profileImage", image, "image/jpeg")
          .accept(ContentType.JSON)
          .post("/api/member/signup");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(201);
            softly.assertThat(response.header("Location")).isNotNull();
        });
    }

    @Test
    @DisplayName("[성공]회원 정보 수정")
    void updateMember_success() {
        // given
        MultiPartSpecBuilder request = new MultiPartSpecBuilder(memberUpdateRequest);
        request.charset("UTF-8");
        request.controlName("memberUpdateRequest");
        request.mimeType("application/json");
        doReturn("mocked_url").when(s3Service)
                              .uploadAndGetImageURL(any(MultipartFile.class), eq(S3Directory.MEMBER));

        // when
        Response response = customGivenWithDocs(updateMemberDocument())
          .contentType("multipart/form-data;charset=UTF-8")
          .header("Authorization", "Bearer " + accessToken)
          .multiPart(request.build())
          .multiPart("profileImage", image, "image/jpeg")
          .accept(ContentType.JSON)
          .put("/api/member");

        System.out.println("response = " + response.asString());

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    @DisplayName("[성공]비밀번호 초기화(변경)")
    void restPassword() {
        // given
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("1!asdasd", "1!qweqwe");

        // when
        Response response = customGivenWithDocs(resetPasswordDocument())
          .contentType(ContentType.JSON)
          .header("Authorization", "Bearer " + accessToken)
          .body(resetPasswordRequest)
          .accept(ContentType.JSON)
          .put("/api/member/reset-password");

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    @DisplayName("[성공]내 정보 조회")
    void getMyInfo() {
        // when
        Response response = customGivenWithDocs(getMemberDetailDocument())
          .header("Authorization", "Bearer " + accessToken)
          .accept(ContentType.JSON)
          .get("/api/member");

        // then
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("[성공]회원 탈퇴")
    void deleteMember() {
        // when
        Response response = customGivenWithDocs(deleteMemberDocument())
          .header("Authorization", "Bearer " + accessToken)
          .accept(ContentType.JSON)
          .delete("/api/member");

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }

}