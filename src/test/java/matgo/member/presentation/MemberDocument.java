package matgo.member.presentation;


import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;

import org.springframework.restdocs.restassured.RestDocumentationFilter;

public class MemberDocument {


    public static RestDocumentationFilter registerMemberDocument() {
        return document("회원가입",
          resourceDetails().tag("Member").description(
            "회원가입/form-data타입이 문서화되지 않습니다..https://documenter.getpostman.com/view/24155473/2s9YsMBBm9 이 링크를 참고해주세요."),
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
          resourceDetails().tag("Member").description(
            "회원 정보 수정//form-data타입이 문서화되지 않습니다..https://documenter.getpostman.com/view/24155473/2s9YsMBBm9 이 링크를 참고해주세요.\"),"),
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

    public static RestDocumentationFilter resetPasswordDocument() {
        return document("비밀번호 초기화",
          resourceDetails().tag("Member").description("비밀번호 초기화"),
          requestFields(
            fieldWithPath("currentPassword").description("현재 비밀번호"),
            fieldWithPath("newPassword").description("새 비밀번호")
          )
        );
    }

    public static RestDocumentationFilter getMemberDetailDocument() {
        return document("회원 상세 조회",
          resourceDetails().tag("Member").description("회원 상세 조회")
        );
    }

    public static RestDocumentationFilter deleteMemberDocument() {
        return document("회원 탈퇴",
          resourceDetails().tag("Member").description("회원 탈퇴")
        );
    }
}
