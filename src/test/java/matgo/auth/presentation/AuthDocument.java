package matgo.auth.presentation;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import org.springframework.restdocs.restassured.RestDocumentationFilter;

public class AuthDocument {

    public static RestDocumentationFilter verifyEmailCodeDocument() {
        return document("이메일 인증",
          resourceDetails().tag("Auth").description("이메일 인증"),
          requestFields(
            fieldWithPath("email").description("이메일"),
            fieldWithPath("code").description("인증 코드")
          )
        );
    }

    public static RestDocumentationFilter loginDocument() {
        return document("로그인",
          resourceDetails().tag("Auth").description("로그인"),
          requestFields(
            fieldWithPath("email").description("이메일"),
            fieldWithPath("password").description("비밀번호"),
            fieldWithPath("role").description("유저 권한")
          )
        );
    }

    public static RestDocumentationFilter logoutDocument() {
        return document("로그아웃",
          resourceDetails().tag("Auth").description("로그아웃")
        );
    }

    public static RestDocumentationFilter sendTemporayPasswordDocument() {
        return document("임시 비밀번호 발급",
          resourceDetails().tag("Auth").description("임시 비밀번호 발급"),
          requestFields(
            fieldWithPath("email").description("이메일")
          )
        );
    }
}
