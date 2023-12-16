package matgo.member.application;

import static matgo.global.exception.ErrorCode.ALREADY_EXISTED_EMAIL;
import static matgo.global.exception.ErrorCode.ALREADY_EXISTED_NICKNAME;
import static matgo.global.exception.ErrorCode.NOT_FOUND_REGION;
import static matgo.member.domain.type.UserRole.USER;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.auth.application.MailService;
import matgo.global.s3.S3Service;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.domain.repository.RegionRepository;
import matgo.member.dto.request.SignUpRequest;
import matgo.member.dto.response.SignUpResponse;
import matgo.member.exception.MemberException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    private static final String S3_DIRECTORY = "member";

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;

    private final S3Service s3Service;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${images.default-profile-image}")
    private String defaultProfileImage;

    public SignUpResponse saveMember(SignUpRequest signUpRequest) {
        validateDuplicateEmail(signUpRequest.email());
        validateDuplicateNickname(signUpRequest.nickname());

        Region region = getRegion(signUpRequest.region());
        String password = passwordEncoder.encode(signUpRequest.password());
        String imageUrl = uploadAndGetImageURL(signUpRequest.profileImage());
        Member member = Member.builder()
                              .email(signUpRequest.email())
                              .nickname(signUpRequest.nickname())
                              .password(password)
                              .profileImage(imageUrl)
                              .role(USER)
                              .region(region)
                              .build();
        memberRepository.save(member);
        mailService.sendVerificationCode(member.getEmail());

        return SignUpResponse.from(member.getId(), member.getEmail());
    }


    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberException(ALREADY_EXISTED_EMAIL);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new MemberException(ALREADY_EXISTED_NICKNAME);
        }
    }

    private Region getRegion(String region) {
        return regionRepository.findByName(region)
                               .orElseThrow(() -> new MemberException(NOT_FOUND_REGION));
    }

    public String uploadAndGetImageURL(MultipartFile profileImage) {
        return Optional.ofNullable(profileImage)
                       .filter(image -> !image.isEmpty())
                       .map(image -> s3Service.upload(
                         image,
                         S3_DIRECTORY,
                         String.valueOf(UUID.randomUUID()),
                         image.getOriginalFilename()))
                       .orElse(defaultProfileImage);
    }

}
