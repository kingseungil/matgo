package matgo.common;

import matgo.auth.application.MailService;
import matgo.auth.domain.repository.EmailVerificationRepository;
import matgo.global.filesystem.s3.S3Service;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.domain.repository.RegionRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseServiceTest {

    @Mock
    protected MemberRepository memberRepository;
    @Mock
    protected RegionRepository regionRepository;
    @Mock
    protected EmailVerificationRepository emailVerificationRepository;
    @Mock
    protected S3Service s3Service;
    @Mock
    protected PasswordEncoder passwordEncoder;
    @Mock
    protected MailService mailService;

}
