package matgo.post.domain.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QList;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matgo.member.dto.response.MemberResponse;
import matgo.post.domain.entity.QPost;
import matgo.post.domain.entity.QPostImage;
import matgo.post.dto.response.PostDetailResponse;
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

    public Optional<PostDetailResponse> findPostDetailResponseById(Long regionId, Long postId) {
        List<PostDetailResponse> postDetailResponses = jpaQueryFactory.select(postDetailProjection())
                                                                      .from(qPost)
                                                                      .join(qPost.member)
                                                                      .leftJoin(qPost.postImages, qPostImage)
                                                                      .where(
                                                                        qPost.id.eq(postId),
                                                                        qPost.region.id.eq(regionId)
                                                                      )
                                                                      .fetch();

        if (postDetailResponses.isEmpty()) {
            return Optional.empty();
        }

        List<String> allImages = mergeImages(postDetailResponses);
        PostDetailResponse response = postDetailResponses.get(0);

        PostDetailResponse mergedResponse = new PostDetailResponse(
          response.post(),
          allImages,
          response.member()
        );

        return Optional.of(mergedResponse);
    }

    public PostSliceResponse findAllPostSliceByRegionId(Long regionId, Pageable pageable) {
        List<PostDetailResponse> postDetailResponses = jpaQueryFactory.select(postDetailProjection())
                                                                      .from(qPost)
                                                                      .join(qPost.member)
                                                                      .leftJoin(qPost.postImages, qPostImage)
                                                                      .where(qPost.region.id.eq(regionId))
                                                                      .orderBy(getOrderSpecifier(pageable.getSort()))
                                                                      .offset(pageable.getOffset())
                                                                      .limit(pageable.getPageSize())
                                                                      .fetch();

        List<PostDetailResponse> responses = postDetailResponses.stream()
                                                                .map(response -> {
                                                                    List<String> allImages = mergeImages(
                                                                      Collections.singletonList(response));
                                                                    return new PostDetailResponse(
                                                                      response.post(),
                                                                      allImages,
                                                                      response.member());
                                                                })
                                                                .toList();

        return new PostSliceResponse(responses, responses.size() == pageable.getPageSize());
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

    private ConstructorExpression<PostDetailResponse> postDetailProjection() {
        return Projections.constructor(PostDetailResponse.class,
          postProjection(),
          postImagesProjection(),
          memberProjection()
        );
    }

    private ConstructorExpression<PostResponse> postProjection() {
        return Projections.constructor(PostResponse.class,
          qPost.id,
          qPost.title,
          qPost.content,
          qPost.likeCount,
          qPost.dislikeCount,
          qPost.region.name,
          qPost.createdAt,
          qPost.modifiedAt
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

    private List<String> mergeImages(List<PostDetailResponse> responses) {
        return responses.stream()
                        .flatMap(response -> response.postImages().stream())
                        .toList();
    }
}
