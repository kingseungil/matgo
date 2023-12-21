package matgo.member.presentation;

import static matgo.member.presentation.MemberDocument.registerMemberDocument;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import matgo.common.BaseControllerTest;
import matgo.member.domain.entity.Region;
import matgo.member.domain.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

class MemberControllerTest extends BaseControllerTest {

    @Autowired
    private RegionRepository regionRepository;

    @BeforeEach
    void setUp() {
        regionRepository.save(new Region("효자동"));
    }
    
    @Test
    @DisplayName("[성공]회원가입")
    void registerMember_success() {
        // given
        MultiPartSpecBuilder request = new MultiPartSpecBuilder(signUpRequest);
        request.charset("UTF-8");
        request.controlName("signUpRequest");
        request.mimeType("application/json");
        doReturn("mocked_url").when(s3Service)
                              .upload(any(MultipartFile.class), any(String.class), any(String.class),
                                any(String.class));

        // when
        Response response = customGivenWithDocs(registerMemberDocument())
          .contentType("multipart/form-data;charset=UTF-8")
          .multiPart(request.build())
          .multiPart("profileImage", image, "image/jpeg")
          .accept(ContentType.JSON)
          .post("/api/member");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(201);
            softly.assertThat(response.header("Location")).isNotNull();
        });
    }
}