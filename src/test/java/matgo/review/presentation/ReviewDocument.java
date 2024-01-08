package matgo.review.presentation;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;

import org.springframework.restdocs.restassured.RestDocumentationFilter;

public class ReviewDocument {

    public static RestDocumentationFilter createReviewDocument() {
        return document("리뷰 작성",
          resourceDetails().tag("Review").description("리뷰 작성"),
          pathParameters(
            parameterWithName("restaurantId").description("식당 아이디")
          ),
          requestParts(
            partWithName("reviewCreateRequest").description("리뷰 등록 정보"),
            partWithName("reviewImage").description("리뷰 이미지").optional()
          ),
          requestPartFields("reviewCreateRequest",
            fieldWithPath("content").description("내용"),
            fieldWithPath("rating").description("평점"),
            fieldWithPath("revisit").description("재방문 의사")
          )
        );
    }

    public static RestDocumentationFilter addReviewReactionDocument() {
        return document("리뷰 좋아요/싫어요",
          resourceDetails().tag("Review").description("리뷰 좋아요/싫어요"),
          pathParameters(
            parameterWithName("reviewId").description("리뷰 아이디")
          ),
          queryParameters(
            parameterWithName("reactionType").description("LIKE/DISLIKE")
          )
        );
    }

    public static RestDocumentationFilter getReviewsDocument() {
        return document("리뷰 조회(페이징)",
          resourceDetails().tag("Review").description("리뷰 조회(페이징)"),
          queryParameters(
            parameterWithName("page").description("페이지 번호"),
            parameterWithName("size").description("페이지 크기"),
            parameterWithName("direction").description("정렬 방향(DESC,ASC)").optional(),
            parameterWithName("sortBy").description("정렬 기준(rating,createdAt,likeCount,dislikeCount)").optional()
          )
        );
    }

    public static RestDocumentationFilter getReviewDetailDocument() {
        return document("리뷰 상세 조회",
          resourceDetails().tag("Review").description("리뷰 상세 조회"),
          pathParameters(
            parameterWithName("reviewId").description("리뷰 아이디")
          )
        );
    }

    public static RestDocumentationFilter deleteReviewDocument() {
        return document("리뷰 삭제",
          resourceDetails().tag("Review").description("리뷰 삭제"),
          pathParameters(
            parameterWithName("restaurantId").description("식당 아이디"),
            parameterWithName("reviewId").description("리뷰 아이디")
          )
        );
    }

    public static RestDocumentationFilter getMyReviewsDocument() {
        return document("내가 쓴 리뷰 조회",
          resourceDetails().tag("Review").description("내가 쓴 리뷰 조회"),
          queryParameters(
            parameterWithName("page").description("페이지 번호"),
            parameterWithName("size").description("페이지 크기"),
            parameterWithName("direction").description("정렬 방향(DESC,ASC)").optional(),
            parameterWithName("sortBy").description("정렬 기준(rating,createdAt,likeCount,dislikeCount)").optional()
          )
        );
    }
}
