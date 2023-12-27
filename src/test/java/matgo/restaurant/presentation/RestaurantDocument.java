package matgo.restaurant.presentation;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import org.springframework.restdocs.restassured.RestDocumentationFilter;

public class RestaurantDocument {


    public static RestDocumentationFilter getRestaurantDetailDocument() {
        return document("식당 상세 조회",
          resourceDetails().tag("Restaurant").description("식당 상세 조회"),
          pathParameters(
            parameterWithName("restaurantId").description("식당 아이디")
          )
        );
    }

    public static RestDocumentationFilter getRestaurantsDocument() {
        return document("전체 식당 목록 조회",
          resourceDetails().tag("Restaurant").description("식당 목록 조회"),
          queryParameters(
            parameterWithName("page").description("페이지 번호"),
            parameterWithName("size").description("페이지 크기")
          )
        );
    }

    public static RestDocumentationFilter getRestaurantsByAddressDocument() {
        return document("주소로 식당 목록 조회",
          resourceDetails().tag("Restaurant").description("주소로 식당 목록 조회"),
          queryParameters(
            parameterWithName("keyword").description("주소 키워드"),
            parameterWithName("page").description("페이지 번호"),
            parameterWithName("size").description("페이지 크기")
          )
        );
    }

    public static RestDocumentationFilter getRestaurantsByRegionDocument() {
        return document("주변 식당 목록 조회",
          resourceDetails().tag("Restaurant").description("주변 식당 목록 조회"),
          queryParameters(
            parameterWithName("page").description("페이지 번호"),
            parameterWithName("size").description("페이지 크기")
          )
        );
    }
}
