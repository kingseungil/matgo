package matgo.common;


import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.specification.RequestSpecification;
import matgo.auth.application.MailService;
import matgo.global.s3.S3Service;
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

    protected RequestSpecification spec;

    @SpyBean
    protected S3Service s3Service;
    @SpyBean
    protected MailService mailService;
    @LocalServerPort
    int port;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        RestAssured.port = port;

        Filter documentConfig = documentationConfiguration(restDocumentation)
          .operationPreprocessors()
          .withRequestDefaults(prettyPrint())
          .withResponseDefaults(prettyPrint());

        this.spec = new RequestSpecBuilder().addFilter(documentConfig).build();
    }

    protected RequestSpecification customGiven() {
        final RequestSpecification customGiven = given();
        return customGiven.log().all();
    }

    protected RequestSpecification customGivenWithDocs(Filter document) {
        final RequestSpecification customGiven = given(spec).filter(document);
        return customGiven.log().all();
    }
}