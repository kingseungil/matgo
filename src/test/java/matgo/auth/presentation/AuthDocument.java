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

}
