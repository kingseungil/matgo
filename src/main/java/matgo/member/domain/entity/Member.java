package matgo.member.domain.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import matgo.auth.domain.entity.EmailVerification;
import matgo.global.entity.BaseEntity;
import matgo.member.domain.type.UserRole;
import matgo.post.domain.entity.Post;
import matgo.post.domain.entity.PostComment;
import matgo.post.domain.entity.PostReaction;
import matgo.review.domain.entity.Review;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
  name = "member",
  uniqueConstraints = {
    @UniqueConstraint(name = "UK_email", columnNames = {"email"}),
    @UniqueConstraint(name = "UK_nickname", columnNames = {"nickname"}),
  }
)
@SQLDelete(sql = "UPDATE member SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "profile_image", nullable = false)
    private String profileImage;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", foreignKey = @ForeignKey(name = "fk_member_region"), nullable = false)
    private Region region;

    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Setter
    private EmailVerification emailVerification;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<PostReaction> postReactions = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Review> reviewReactions = new ArrayList<>();

    public void verifyEmail() {
        this.isActive = true;
    }

    public boolean isVerified() {
        return this.isActive;
    }

    public void changeProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeRegion(Region region) {
        this.region = region;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    public void addPostComment(PostComment postComment) {
        this.postComments.add(postComment);
    }

    public void removePostComment(PostComment postComment) {
        this.postComments.remove(postComment);
    }
}
