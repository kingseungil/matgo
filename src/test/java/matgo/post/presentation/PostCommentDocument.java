package matgo.post.presentation;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.resourceDetails;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.restassured.RestDocumentationFilter;

public class PostCommentDocument {

    public static RestDocumentationFilter createCommentDocument() {
        return document("댓글 작성",
          resourceDetails().tag("PostComment").description("댓글 작성"),
          pathParameters(
            RequestDocumentation.parameterWithName("postId").description("게시글 아이디")
          ),
          requestFields(
            fieldWithPath("content").description("내용")
          )
        );
    }

    public static RestDocumentationFilter updateCommentDocument() {
        return document("댓글 수정",
          resourceDetails().tag("PostComment").description("댓글 수정"),
          pathParameters(
            RequestDocumentation.parameterWithName("commentId").description("댓글 아이디")
          ),
          requestFields(
            fieldWithPath("content").description("내용")
          )
        );
    }

    public static RestDocumentationFilter deleteCommentDocument() {
        return document("댓글 삭제",
          resourceDetails().tag("PostComment").description("댓글 삭제"),
          pathParameters(
            RequestDocumentation.parameterWithName("commentId").description("댓글 아이디")
          )
        );
    }

    public static RestDocumentationFilter getMyPostCommentDocument() {
        return document("댓글 조회(페이징)",
          resourceDetails().tag("PostComment").description("내가 작성한 댓글 조회"),
          queryParameters(
            parameterWithName("page").description("페이지 번호"),
            parameterWithName("size").description("페이지 크기"),
            parameterWithName("direction").description("정렬 방향(DESC,ASC)").optional(),
            parameterWithName("sortBy").description("정렬 기준(createdAt)").optional()
          )
        );
    }
}
