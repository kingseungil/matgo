package matgo.common;


import static io.restassured.RestAssured.given;
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
import matgo.global.s3.S3Service;
import matgo.member.domain.type.UserRole;
import matgo.member.dto.request.SignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(RestDocumentationExtension.class)
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    protected static final File image = new File("src/test/resources/img.jpeg");
    protected static final SignUpRequest signUpRequest = new SignUpRequest("test@naver.com", "testnick", "1!asdasd",
      "효자동");
    protected static final LoginRequest loginRequest = new LoginRequest("test@naver.com", "1!asdasd",
      UserRole.ROLE_USER);


    protected RequestSpecification spec;

    @SpyBean
    protected S3Service s3Service;
    @SpyBean
    protected MailService mailService;
    @SpyBean
    protected AuthService authService;
    @LocalServerPort
    int port;

    protected static RequestSpecification customGiven() {
        final RequestSpecification customGiven = given();
        return customGiven.log().all();
    }

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        RestAssured.port = port;

        Filter documentConfig = documentationConfiguration(restDocumentation)
          .operationPreprocessors()
          .withRequestDefaults(prettyPrint())
          .withResponseDefaults(prettyPrint());

        this.spec = new RequestSpecBuilder().addFilter(documentConfig).build();
    }

    protected RequestSpecification customGivenWithDocs(Filter document) {
        final RequestSpecification customGiven = given(spec).filter(document);
        return customGiven.log().all();
    }
}