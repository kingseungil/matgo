package matgo.post.presentation;

import static matgo.post.presentation.PostDocument.createPostDocument;
import static matgo.post.presentation.PostDocument.deletePostDocument;
import static matgo.post.presentation.PostDocument.getPostDetailDocument;
import static matgo.post.presentation.PostDocument.getPostsDocument;
import static matgo.post.presentation.PostDocument.updatePostDocument;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Optional;
import matgo.common.BaseControllerTest;
import matgo.global.type.S3Directory;
import matgo.post.domain.entity.Post;
import matgo.post.dto.response.PostDetailResponse;
import matgo.post.dto.response.PostSliceResponse;
import matgo.restaurant.dto.request.CustomPageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.multipart.MultipartFile;

class PostControllerTest extends BaseControllerTest {

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
    }

    @Test
    @DisplayName("[성공]게시글 작성")
    void createPost() {
        // given
        MultiPartSpecBuilder request = new MultiPartSpecBuilder(postCreateRequest)
          .controlName("postCreateRequest")
          .charset("UTF-8")
          .mimeType("application/json");
        doReturn("mocked_url").when(s3Service)
                              .uploadAndGetImageURL(any(MultipartFile.class), eq(S3Directory.POST));
        // when
        Response response = customGivenWithDocs(createPostDocument())
          .contentType("multipart/form-data;charset=UTF-8")
          .header("Authorization", "Bearer " + accessToken)
          .multiPart(request.build())
          .multiPart("postImages", image, "image/jpeg")
          .accept(ContentType.JSON)
          .post("/api/posts/new");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(201);
            softly.assertThat(response.header("Location")).isNotNull();
        });
    }

    @Test
    @DisplayName("[성공]게시글 수정")
    void updatePost() {
        // given
        Long postId = 1L;
        MultiPartSpecBuilder request = new MultiPartSpecBuilder(postUpdateRequest)
          .controlName("postUpdateRequest")
          .charset("UTF-8")
          .mimeType("application/json");
        doReturn("mocked_url").when(s3Service)
                              .uploadAndGetImageURL(any(MultipartFile.class), eq(S3Directory.POST));
        // when
        Response response = customGivenWithDocs(updatePostDocument())
          .contentType("multipart/form-data;charset=UTF-8")
          .pathParam("postId", postId)
          .header("Authorization", "Bearer " + accessToken)
          .multiPart(request.build())
          .multiPart("postImages", image, "image/jpeg")
          .accept(ContentType.JSON)
          .put("/api/posts/{postId}", postId);

        // then
        assertSoftly(softly -> softly.assertThat(response.statusCode()).isEqualTo(204));
    }

    @Test
    @DisplayName("[성공]게시글 삭제")
    void deletePost() {
        // given
        Long postId = 1L;

        // when
        Response response = customGivenWithDocs(deletePostDocument())
          .contentType(ContentType.JSON)
          .pathParam("postId", postId)
          .header("Authorization", "Bearer " + accessToken)
          .delete("/api/posts/{postId}", postId);

        // then
        assertSoftly(softly -> softly.assertThat(response.statusCode()).isEqualTo(204));
    }

    @Test
    @DisplayName("[성공]게시글 상세 조회")
    void getPostDetail() {
        // given
        Long postId = 1L;

        // when
        Response response = customGivenWithDocs(getPostDetailDocument())
          .contentType(ContentType.JSON)
          .header("Authorization", "Bearer " + accessToken)
          .pathParam("postId", postId)
          .accept(ContentType.JSON)
          .get("/api/posts/detail/{postId}", postId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            PostDetailResponse postDetailResponse = response.as(PostDetailResponse.class);
            softly.assertThat(postDetailResponse.post().postId()).isEqualTo(postId);
            softly.assertThat(postDetailResponse.post().title()).isEqualTo(post.getTitle());
            softly.assertThat(postDetailResponse.member().id()).isEqualTo(member.getId());
        });
    }

    @Test
    @DisplayName("[성공]게시글 목록 조회")
    void getPosts() {
        // given
        // when
        Response response = customGivenWithDocs(getPostsDocument())
          .contentType(ContentType.JSON)
          .header("Authorization", "Bearer " + accessToken)
          .queryParam("page", customPageRequest.page())
          .queryParam("size", customPageRequest.size())
          .queryParam("direction", customPageRequest.direction().get())
          .queryParam("sortBy", customPageRequest.sortBy().get())
          .accept(ContentType.JSON)
          .get("/api/posts");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            PostSliceResponse postSliceResponse = response.as(PostSliceResponse.class);
            softly.assertThat(postSliceResponse.posts()).hasSize(1);
            softly.assertThat(postSliceResponse.posts().get(0).post().postId()).isEqualTo(post.getId());
            softly.assertThat(postSliceResponse.posts().get(0).post().title()).isEqualTo(post.getTitle());
            softly.assertThat(postSliceResponse.posts().get(0).member().id()).isEqualTo(member.getId());
        });
    }

//    @Test
//    @DisplayName("[성공]게시글 좋아요/싫어요")
}