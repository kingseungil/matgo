package matgo.review.domain.entity;

import static matgo.global.exception.ErrorCode.CANNOT_DECREASE_DISLIKE_COUNT;
import static matgo.global.exception.ErrorCode.CANNOT_DECREASE_LIKE_COUNT;

import jakarta.persistence.CascadeType;
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
import matgo.review.exception.ReviewException;

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

    @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ReviewReaction> reviewReactions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_review_member"), nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", foreignKey = @ForeignKey(name = "fk_review_restaurant"), nullable = false)
    private Restaurant restaurant;

    public boolean hasReaction(Member member) {
        return reviewReactions.stream()
                              .anyMatch(reviewReaction -> reviewReaction.getMember().equals(member));
    }

    public void addReviewReaction(ReviewReaction reviewReaction) {
        this.reviewReactions.add(reviewReaction);
    }

    public void removeReviewReaction(ReviewReaction reviewReaction) {
        this.reviewReactions.remove(reviewReaction);
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        validationLikeCount();
        this.likeCount--;
    }

    private void validationLikeCount() {
        if (this.likeCount <= 0) {
            throw new ReviewException(CANNOT_DECREASE_LIKE_COUNT);
        }
    }

    public void increaseDislikeCount() {
        this.dislikeCount++;
    }

    public void decreaseDislikeCount() {
        validationDislikeCount();
        this.dislikeCount--;
    }

    private void validationDislikeCount() {
        if (this.dislikeCount <= 0) {
            throw new ReviewException(CANNOT_DECREASE_DISLIKE_COUNT);
        }
    }
}
