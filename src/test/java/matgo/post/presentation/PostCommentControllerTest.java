package matgo.post.presentation;

import static matgo.post.presentation.PostCommentDocument.createCommentDocument;
import static matgo.post.presentation.PostCommentDocument.deleteCommentDocument;
import static matgo.post.presentation.PostCommentDocument.getMyPostCommentDocument;
import static matgo.post.presentation.PostCommentDocument.updateCommentDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.restassured.response.Response;
import java.util.Optional;
import matgo.common.BaseControllerTest;
import matgo.post.domain.entity.Post;
import matgo.post.domain.entity.PostComment;
import matgo.post.dto.request.PostCommentCreateRequest;
import matgo.post.dto.request.PostCommentUpdateRequest;
import matgo.post.dto.response.PostCommentSliceResponse;
import matgo.restaurant.dto.request.CustomPageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;

class PostCommentControllerTest extends BaseControllerTest {

    CustomPageRequest customPageRequest = new CustomPageRequest(0, 10, Optional.of(Direction.DESC),
      Optional.of("createdAt"));
    private Post post;

    @BeforeEach
    void setUp() {
        post = Post.builder()
                   .id(1L)
                   .title("title")
                   .content("content")
                   .member(member)
                   .region(region)
                   .likeCount(0)
                   .dislikeCount(0)
                   .build();
        postRepository.save(post);

        PostComment postComment = PostComment.builder()
                                             .id(1L)
                                             .content("content")
                                             .member(member)
                                             .post(post)
                                             .build();
        postCommentRepository.save(postComment);
    }

    @Test
    @DisplayName("댓글 작성")
    void createComment() {
        // given
        Long postId = post.getId();
        PostCommentCreateRequest postCommentCreateRequest = new PostCommentCreateRequest("content");

        // when
        Response response = customGivenWithDocs(createCommentDocument())
          .contentType("application/json")
          .header("Authorization", "Bearer " + accessToken)
          .body(postCommentCreateRequest)
          .accept("application/json")
          .post("/api/comments/{postId}", postId);

        // then
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("댓글 수정")
    void updateComment() {
        // given
        Long commentId = 1L;
        PostCommentUpdateRequest postCommentUpdateRequest = new PostCommentUpdateRequest("updated content");

        // when
        Response response = customGivenWithDocs(updateCommentDocument())
          .contentType("application/json")
          .header("Authorization", "Bearer " + accessToken)
          .body(postCommentUpdateRequest)
          .accept("application/json")
          .put("/api/comments/{commentId}", commentId);

        // then
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() {
        // given
        Long commentId = 1L;

        // when
        Response response = customGivenWithDocs(deleteCommentDocument())
          .contentType("application/json")
          .header("Authorization", "Bearer " + accessToken)
          .accept("application/json")
          .delete("/api/comments/{commentId}", commentId);

        // then
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("내가 작성한 댓글 목록 조회")
    void getComments() {
        // given
        // when
        Response response = customGivenWithDocs(getMyPostCommentDocument())
          .contentType("application/json")
          .header("Authorization", "Bearer " + accessToken)
          .queryParam("page", customPageRequest.page())
          .queryParam("size", customPageRequest.size())
          .queryParam("direction", customPageRequest.direction().get())
          .queryParam("sortBy", customPageRequest.sortBy().get())
          .accept("application/json")
          .get("/api/comments/my/writable-comments");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            PostCommentSliceResponse postCommentSliceResponse = response.as(PostCommentSliceResponse.class);
            softly.assertThat(postCommentSliceResponse.comments().size()).isEqualTo(1);
            softly.assertThat(postCommentSliceResponse.comments().get(0).id()).isEqualTo(1L);
        });
    }

}