package matgo.post.domain.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matgo.post.domain.entity.QPostComment;
import matgo.post.dto.response.PostCommentResponse;
import matgo.post.dto.response.PostCommentSliceResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostCommentQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QPostComment qPostComment = QPostComment.postComment;

    public PostCommentSliceResponse findAllByMemberId(Long memberId, Pageable pageable) {
        List<PostCommentResponse> responses = jpaQueryFactory.select(postCommentProjection())
                                                             .from(qPostComment)
                                                             .where(qPostComment.member.id.eq(memberId))
                                                             .orderBy(getOrderSpecifier(pageable.getSort()))
                                                             .offset(pageable.getOffset())
                                                             .limit(pageable.getPageSize())
                                                             .fetch();

        return new PostCommentSliceResponse(responses, responses.size() == pageable.getPageSize());
    }

    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        return sort.stream()
                   .map(order -> switch (order.getProperty()) {
                       case "createdAt" ->
                         order.isAscending() ? qPostComment.createdAt.asc() : qPostComment.createdAt.desc();
                       default -> qPostComment.createdAt.desc();
                   })
                   .toArray(OrderSpecifier[]::new);
    }

    private ConstructorExpression<PostCommentResponse> postCommentProjection() {
        return Projections.constructor(PostCommentResponse.class,
          qPostComment.id,
          qPostComment.content,
          qPostComment.createdAt,
          qPostComment.modifiedAt
        );
    }


}
