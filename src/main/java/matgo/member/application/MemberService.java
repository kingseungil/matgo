package matgo.member.application;

import static matgo.global.exception.ErrorCode.ALREADY_EXISTED_EMAIL;
import static matgo.global.exception.ErrorCode.ALREADY_EXISTED_NICKNAME;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_REGION;
import static matgo.global.exception.ErrorCode.WRONG_PASSWORD;
import static matgo.member.domain.type.UserRole.ROLE_USER;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.auth.application.MailService;
import matgo.auth.domain.entity.EmailVerification;
import matgo.auth.domain.repository.EmailVerificationRepository;
import matgo.auth.exception.AuthException;
import matgo.global.filesystem.s3.S3Service;
import matgo.global.type.S3Directory;
import matgo.global.util.S3Util;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.domain.repository.RegionRepository;
import matgo.member.dto.request.MemberUpdateRequest;
import matgo.member.dto.request.ResetPasswordRequest;
import matgo.member.dto.request.SignUpRequest;
import matgo.member.dto.response.SignUpResponse;
import matgo.member.exception.MemberException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    private final S3Service s3Service;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final S3Util s3Util;


    public SignUpResponse saveMember(SignUpRequest signUpRequest, MultipartFile profileImage) {
        validateDuplicateEmail(signUpRequest.email());
        validateDuplicateNickname(signUpRequest.nickname());

        Region region = getRegion(signUpRequest.region());
        String password = passwordEncoder.encode(signUpRequest.password());
        String imageUrl = s3Util.uploadAndGetImageURL(profileImage, S3Directory.MEMBER);
        Member member = Member.builder()
                              .email(signUpRequest.email())
                              .nickname(signUpRequest.nickname())
                              .password(password)
                              .profileImage(imageUrl)
                              .role(ROLE_USER)
                              .region(region)
                              .build();
        memberRepository.save(member);
        String verificationCode = mailService.sendVerificationCode(member.getEmail());
        saveEmailVerification(verificationCode, member);

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


    private void saveEmailVerification(String verificationCode, Member member) {
        EmailVerification emailVerification = new EmailVerification(verificationCode,
          LocalDateTime.now().plusHours(24), member);
        emailVerificationRepository.save(emailVerification);
    }


    public void updateMember(Long memberId, MemberUpdateRequest memberUpdateRequest, MultipartFile profileImage) {
        Member member = getMemberById(memberId);

        updateProfileImageIfPresent(member, profileImage);
        updateNicknameIfChanged(member, memberUpdateRequest.nickname());
        updateRegionIfChanged(member, memberUpdateRequest.region());

    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                               .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    private void updateProfileImageIfPresent(Member member, MultipartFile profileImage) {
        if (profileImage != null && !profileImage.isEmpty()) {
            String newImageURL = s3Util.uploadAndGetImageURL(profileImage, S3Directory.MEMBER);
            String oldImageURL = member.getProfileImage();
            member.changeProfileImage(newImageURL);
            s3Service.delete(oldImageURL);
        }
    }

    private void updateNicknameIfChanged(Member member, String newNickname) {
        if (newNickname != null && !newNickname.isEmpty()) {
            validateDuplicateNickname(newNickname);
            member.changeNickname(newNickname);
        }
    }

    private void updateRegionIfChanged(Member member, String newRegionName) {
        if (newRegionName != null && !newRegionName.isEmpty()) {
            Region region = getRegion(newRegionName);
            member.changeRegion(region);
        }
    }

    public void resetPassword(Long memberId, ResetPasswordRequest resetPasswordRequest) {
        Member member = getMemberById(memberId);

        if (!passwordEncoder.matches(resetPasswordRequest.currentPassword(), member.getPassword())) {
            throw new AuthException(WRONG_PASSWORD);
        }

        String newPassword = passwordEncoder.encode(resetPasswordRequest.newPassword());
        member.changePassword(newPassword);
    }
}
