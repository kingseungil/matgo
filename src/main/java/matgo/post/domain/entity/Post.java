package matgo.post.domain.entity;

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
    private List<PostReaction> postReaction = new ArrayList<>();

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
}
