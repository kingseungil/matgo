package matgo.post.domain.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QList;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matgo.member.dto.response.MemberResponse;
import matgo.post.domain.entity.QPost;
import matgo.post.domain.entity.QPostComment;
import matgo.post.domain.entity.QPostImage;
import matgo.post.dto.response.PostCommentResponse;
import matgo.post.dto.response.PostDetailResponse;
import matgo.post.dto.response.PostListResponse;
import matgo.post.dto.response.PostResponse;
import matgo.post.dto.response.PostSliceResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QPost qPost = QPost.post;
    private final QPostImage qPostImage = QPostImage.postImage;
    private final QPostComment qPostComment = QPostComment.postComment;

    public Optional<PostDetailResponse> findPostDetailResponseById(Long regionId, Long postId) {
        List<PostDetailResponse> postDetailResponses = jpaQueryFactory.select(postDetailProjection())
                                                                      .from(qPost)
                                                                      .join(qPost.member)
                                                                      .leftJoin(qPost.postImages, qPostImage)
                                                                      .leftJoin(qPost.postComments, qPostComment)
                                                                      .where(
                                                                        qPost.id.eq(postId),
                                                                        qPost.region.id.eq(regionId)
                                                                      )
                                                                      .fetch();

        if (postDetailResponses.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mergePostDetailResponse(postDetailResponses));
    }

    private PostDetailResponse mergePostDetailResponse(List<PostDetailResponse> postDetailResponses) {
        List<String> allImages = mergeImages(postDetailResponses);
        List<PostCommentResponse> allComments = mergeComments(postDetailResponses);
        PostDetailResponse response = postDetailResponses.get(0);

        return new PostDetailResponse(
          response.post(),
          allImages,
          response.member(),
          allComments
        );
    }

    private List<String> mergeImages(List<PostDetailResponse> responses) {
        return responses.stream()
                        .flatMap(response -> response.postImages().stream())
                        .distinct()
                        .toList();
    }

    private List<PostCommentResponse> mergeComments(List<PostDetailResponse> responses) {
        return responses.stream()
                        .flatMap(response -> response.postComments().stream())
                        .distinct()
                        .toList();
    }


    private ConstructorExpression<PostDetailResponse> postDetailProjection() {
        return Projections.constructor(PostDetailResponse.class,
          postProjection(),
          postImagesProjection(),
          memberProjection(),
          Projections.list(postCommentProjection())
        );
    }

    private ConstructorExpression<PostResponse> postProjection() {
        return Projections.constructor(PostResponse.class,
          qPost.id,
          qPost.title,
          qPost.content,
          qPost.likeCount,
          qPost.dislikeCount,
          qPost.commentCount,
          qPost.region.name,
          qPost.createdAt,
          qPost.modifiedAt
        );
    }

    private ConstructorExpression<PostCommentResponse> postCommentProjection() {
        return Projections.constructor(PostCommentResponse.class,
          qPostComment.id,
          qPostComment.content,
          qPostComment.createdAt,
          qPostComment.modifiedAt
        );
    }

    private QList postImagesProjection() {
        return Projections.list(qPostImage.imageUrl);
    }

    private ConstructorExpression<MemberResponse> memberProjection() {
        return Projections.constructor(MemberResponse.class,
          qPost.member.id,
          qPost.member.profileImage,
          qPost.member.nickname);
    }

    public PostSliceResponse findAllPostSliceByRegionId(Long regionId, Pageable pageable) {
        List<PostListResponse> postListResponses = jpaQueryFactory.select(postListProjection())
                                                                  .from(qPost)
                                                                  .join(qPost.member)
                                                                  .where(qPost.region.id.eq(regionId))
                                                                  .orderBy(getOrderSpecifier(pageable.getSort()))
                                                                  .offset(pageable.getOffset())
                                                                  .limit(pageable.getPageSize())
                                                                  .fetch();

        return new PostSliceResponse(postListResponses, postListResponses.size() == pageable.getPageSize());
    }


    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        return sort.stream()
                   .map(order -> switch (order.getProperty()) {
                       case "createdAt" ->
                         order.isAscending() ? QPost.post.createdAt.asc() : QPost.post.createdAt.desc();
                       case "likeCount" ->
                         order.isAscending() ? QPost.post.likeCount.asc() : QPost.post.likeCount.desc();
                       case "dislikeCount" ->
                         order.isAscending() ? QPost.post.dislikeCount.asc() : QPost.post.dislikeCount.desc();
                       default -> QPost.post.createdAt.desc();
                   })
                   .toArray(OrderSpecifier[]::new);
    }

    private ConstructorExpression<PostListResponse> postListProjection() {
        return Projections.constructor(PostListResponse.class,
          postProjection(),
          memberProjection()
        );
    }


}
