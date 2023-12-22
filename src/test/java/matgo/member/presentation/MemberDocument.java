package matgo.member.presentation;


import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;

import org.springframework.restdocs.restassured.RestDocumentationFilter;

public class MemberDocument {


    public static RestDocumentationFilter registerMemberDocument() {
        return document("회원가입",
          resourceDetails().tag("Member").description("회원가입"),
          requestParts(
            partWithName("signUpRequest").description("회원 가입 정보"),
            partWithName("profileImage").description("프로필 이미지").optional()
          ),
          requestPartFields("signUpRequest",
            fieldWithPath("email").description("이메일"),
            fieldWithPath("nickname").description("닉네임"),
            fieldWithPath("password").description("비밀번호"),
            fieldWithPath("region").description("지역")
          )
        );
    }

    public static RestDocumentationFilter updateMemberDocument() {
        return document("회원 정보 수정",
          resourceDetails().tag("Member").description("회원 정보 수정"),
          requestParts(
            partWithName("memberUpdateRequest").description("회원 정보 수정 정보"),
            partWithName("profileImage").description("프로필 이미지").optional()
          ),
          requestPartFields("memberUpdateRequest",
            fieldWithPath("nickname").description("닉네임"),
            fieldWithPath("region").description("지역")
          )
        );
    }

}
