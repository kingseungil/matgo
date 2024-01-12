package matgo.post.presentation;

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

public class PostDocument {

    public static RestDocumentationFilter createPostDocument() {
        return document("게시글 작성",
          resourceDetails().tag("Post").description(
            "게시글 작성//form-data타입이 문서화되지 않습니다..https://documenter.getpostman.com/view/24155473/2s9YsMBBm9 이 링크를 참고해주세요.\"),"),
          requestParts(
            partWithName("postCreateRequest").description("게시글 등록 정보"),
            partWithName("postImages").description("게시글 이미지(최대3장)").optional()
          ),
          requestPartFields("postCreateRequest",
            fieldWithPath("title").description("제목"),
            fieldWithPath("content").description("내용")
          )
        );
    }

    public static RestDocumentationFilter updatePostDocument() {
        return document("게시글 수정",
          resourceDetails().tag("Post").description(
            "게시글 수정//form-data타입이 문서화되지 않습니다..https://documenter.getpostman.com/view/24155473/2s9YsMBBm9 이 링크를 참고해주세요.\"),"),
          pathParameters(
            parameterWithName("postId").description("게시글 아이디")
          ),
          requestParts(
            partWithName("postUpdateRequest").description("게시글 수정 정보"),
            partWithName("postImages").description("게시글 이미지(최대3장)").optional()
          ),
          requestPartFields("postUpdateRequest",
            fieldWithPath("title").description("제목"),
            fieldWithPath("content").description("내용")
          )
        );
    }

    public static RestDocumentationFilter deletePostDocument() {
        return document("게시글 삭제",
          resourceDetails().tag("Post").description("게시글 삭제"),
          pathParameters(
            parameterWithName("postId").description("게시글 아이디")
          )
        );
    }

    public static RestDocumentationFilter getPostDetailDocument() {
        return document("게시글 상세 조회",
          resourceDetails().tag("Post").description("게시글 상세 조회"),
          pathParameters(
            parameterWithName("postId").description("게시글 아이디")
          )
        );
    }

    public static RestDocumentationFilter getPostsDocument() {
        return document("게시글 조회(페이징)",
          resourceDetails().tag("Post").description("게시글 조회(페이징)"),
          queryParameters(
            parameterWithName("page").description("페이지 번호"),
            parameterWithName("size").description("페이지 크기"),
            parameterWithName("direction").description("정렬 방향(DESC,ASC)").optional(),
            parameterWithName("sortBy").description("정렬 기준(createdAt,likeCount,dislikeCount)").optional()
          )
        );
    }

    public static RestDocumentationFilter addPostReactionDocument() {
        return document("게시글 좋아요/싫어요",
          resourceDetails().tag("Post").description("게시글 좋아요/싫어요"),
          pathParameters(
            parameterWithName("postId").description("게시글 아이디")
          ),
          queryParameters(
            parameterWithName("reactionType").description("LIKE/DISLIKE")
          )
        );
    }
}
