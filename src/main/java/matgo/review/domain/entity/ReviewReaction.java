package matgo.review.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matgo.global.entity.BaseEntity;
import matgo.global.type.Reaction;
import matgo.member.domain.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "review_reaction")
public class ReviewReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Reaction reaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", foreignKey = @ForeignKey(name = "fk_reaction_review"), nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_review_reaction_member"), nullable = false)
    private Member member;

    public static ReviewReaction from(Review review, Member member, Reaction reaction) {
        return ReviewReaction.builder()
                             .review(review)
                             .member(member)
                             .reaction(reaction)
                             .build();
    }

    public void changeReaction(Reaction reaction) {
        this.reaction = reaction;
    }
}
