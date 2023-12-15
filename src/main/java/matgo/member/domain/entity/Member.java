package matgo.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matgo.global.entity.BaseEntity;
import matgo.member.domain.type.UserRole;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    private static final String DEFAULT_PROFILE_IMAGE = "https://dthezntil550i.cloudfront.net/pn/latest/pn1608281849186400000834203/1280_960/468201b8-3f90-4f98-b8ae-06aa4f156741.png";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "nickname", nullable = false, unique = true)
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

    @OneToOne(fetch = FetchType.LAZY)
    private Region region;

    @OneToOne(mappedBy = "member")
    private EmailVerification emailVerification;

    @Builder
    // TODO : 매개변수 DTO로 변경
    public Member(String email, String nickname, String password, String profileImage, UserRole role) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.profileImage = (profileImage != null) ? profileImage : DEFAULT_PROFILE_IMAGE;
        this.role = (role != null) ? role : UserRole.USER;
    }
}
