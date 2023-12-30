package matgo.review.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matgo.global.entity.BaseEntity;
import matgo.member.domain.entity.Member;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.review.dto.request.ReviewCreateRequest;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "review")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "revisit", nullable = false)
    private boolean revisit;

    @Column(name = "like_count", nullable = false, columnDefinition = "int default 0")
    private int likeCount;

    @Column(name = "dislike_count", nullable = false, columnDefinition = "int default 0")
    private int dislikeCount;

    @OneToMany(mappedBy = "review")
    private List<ReviewReaction> reviewReactions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_review_member"), nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", foreignKey = @ForeignKey(name = "fk_review_restaurant"), nullable = false)
    private Restaurant restaurant;

    public static Review from(Member member, Restaurant restaurant, ReviewCreateRequest request, String imageUrl) {
        return Review.builder()
                     .member(member)
                     .restaurant(restaurant)
                     .content(request.content())
                     .rating(request.rating())
                     .imageUrl(imageUrl)
                     .revisit(request.revisit())
                     .build();
    }

}
