package matgo.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matgo.global.entity.BaseEntity;
import matgo.member.domain.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "email_verification")
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private String verificationCode;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_email_verification_member"), nullable = false)
    private Member member;

    public EmailVerification(String verificationCode, LocalDateTime expiredAt, Member member) {
        this.verificationCode = verificationCode;
        this.expiredAt = expiredAt;
        this.member = member;
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

}
