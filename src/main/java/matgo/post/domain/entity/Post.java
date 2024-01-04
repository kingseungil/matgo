package matgo.post.domain.entity;

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
import matgo.comment.domain.entity.Comment;
import matgo.global.entity.BaseEntity;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.review.exception.ReviewException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "post")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "like_count", nullable = false, columnDefinition = "int default 0")
    private int likeCount;

    @Column(name = "dislike_count", nullable = false, columnDefinition = "int default 0")
    private int dislikeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", foreignKey = @ForeignKey(name = "fk_post_region"), nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_post_member"), nullable = false)
    private Member member;

    @OneToMany(mappedBy = "post", cascade = CascadeType.PERSIST)
    private List<PostImage> postImages = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostReaction> postReactions = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Comment> comment = new ArrayList<>();

    public void addPostImages(List<PostImage> postImages) {
        this.postImages.addAll(postImages);
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean hasReaction(Member member) {
        return postReactions.stream()
                            .anyMatch(postReaction -> postReaction.getMember().equals(member));
    }

    public void addPostReaction(PostReaction postReaction) {
        this.postReactions.add(postReaction);
    }

    public void removePostReaction(PostReaction postReaction) {
        this.postReactions.remove(postReaction);
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
