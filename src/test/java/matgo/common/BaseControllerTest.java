package matgo.common;


import static io.restassured.RestAssured.given;
import static matgo.auth.presentation.AuthControllerTest.loginMember;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import matgo.auth.application.AuthService;
import matgo.auth.application.MailService;
import matgo.auth.dto.request.LoginRequest;
import matgo.auth.dto.request.SendTemporaryPasswordRequest;
import matgo.global.filesystem.s3.S3Service;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.domain.repository.RegionRepository;
import matgo.member.domain.type.UserRole;
import matgo.member.dto.request.MemberUpdateRequest;
import matgo.member.dto.request.SignUpRequest;
import matgo.restaurant.domain.repository.RestaurantRepository;
import matgo.restaurant.domain.repository.RestaurantSearchRepository;
import matgo.restaurant.domain.repository.RestaurantSearchRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(RestDocumentationExtension.class)
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    protected static final File image = new File("src/test/resources/img.jpeg");
    protected static final SignUpRequest signUpRequest = new SignUpRequest("signup@naver.com", "signup", "1!asdasd",
      "효자동");
    protected static final LoginRequest loginRequest = new LoginRequest("test@naver.com", "1!asdasd",
      UserRole.ROLE_USER);
    protected static final LoginRequest adminLoginRequest = new LoginRequest("admin@admin.com", "1!asdasd",
      UserRole.ROLE_ADMIN);
    protected static final MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest("updateTest", "효자동");
    protected static final SendTemporaryPasswordRequest sendTemporaryPasswordRequest = new SendTemporaryPasswordRequest(
      "test@naver.com");
    protected static RequestSpecification spec;
    protected static String accessToken;
    protected static String adminAccessToken;
    @MockBean
    protected S3Service s3Service;
    @MockBean
    protected MailService mailService;
    @Autowired
    protected AuthService authService;
    @Autowired
    protected RegionRepository regionRepository;
    @Autowired
    protected MemberRepository memberRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected RestaurantRepository restaurantRepository;
    @Autowired
    protected RestaurantSearchRepository restaurantSearchRepository;
    @Autowired
    protected RestaurantSearchRepositoryImpl restaurantSearchRepositoryImpl;
    @LocalServerPort
    int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    protected static RequestSpecification customGiven() {
        final RequestSpecification customGiven = given();
        return customGiven.log().all();
    }

    protected static RequestSpecification customGivenWithDocs(Filter document) {
        final RequestSpecification customGiven = given(spec).filter(document);
        return customGiven.log().all();
    }

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        RestAssured.port = port;

        Filter documentConfig = documentationConfiguration(restDocumentation)
          .operationPreprocessors()
          .withRequestDefaults(prettyPrint())
          .withResponseDefaults(prettyPrint());

        spec = new RequestSpecBuilder().addFilter(documentConfig).build();

        Region region = regionRepository.save(new Region("효자동"));
        String password = passwordEncoder.encode("1!asdasd");
        memberRepository.save(Member.builder()
                                    .email("test@naver.com")
                                    .nickname("testnick")
                                    .password(password)
                                    .profileImage(
                                      "https://matgo-bucket.s3.ap-northeast-2.amazonaws.com/matgo/member/default_image")
                                    .role(UserRole.ROLE_USER)
                                    .region(region)
                                    .isActive(true)
                                    .build());
        memberRepository.save(Member.builder()
                                    .email("admin@admin.com")
                                    .nickname("admin")
                                    .password(password)
                                    .profileImage(
                                      "https://matgo-bucket.s3.ap-northeast-2.amazonaws.com/matgo/member/default_image")
                                    .role(UserRole.ROLE_ADMIN)
                                    .region(region)
                                    .isActive(true)
                                    .build());
        accessToken = loginMember(loginRequest).jsonPath().getString("accessToken");
        adminAccessToken = loginMember(adminLoginRequest).jsonPath().getString("accessToken");

        doReturn("code").when(mailService).sendVerificationCode(anyString());
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.execute();
    }
}