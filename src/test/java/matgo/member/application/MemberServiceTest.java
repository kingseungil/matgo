package matgo.member.application;

import static matgo.global.exception.ErrorCode.ALREADY_EXISTED_NICKNAME;
import static matgo.global.exception.ErrorCode.NOT_FOUND_REGION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import matgo.auth.domain.entity.EmailVerification;
import matgo.common.BaseServiceTest;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
import matgo.member.domain.type.UserRole;
import matgo.member.dto.request.MemberUpdateRequest;
import matgo.member.dto.request.SignUpRequest;
import matgo.member.dto.response.SignUpResponse;
import matgo.member.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class MemberServiceTest extends BaseServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("saveMember 메서드는")
    class saveMember {

        Region region = new Region("효자동");
        SignUpRequest signUpRequest = new SignUpRequest("test@naver.com", "testnick", "1!asdasd", "효자동");
        MultipartFile profileImage = new MockMultipartFile("profileImage", "img.jpeg", "image/jpeg",
          "image data".getBytes(StandardCharsets.UTF_8));

        @Test
        @DisplayName("회원가입에 성공하면 유저 이메일을 반환한다.")
        void success() {
            // given
            doReturn(false).when(memberRepository).existsByEmail(anyString());
            doReturn(false).when(memberRepository).existsByNickname(anyString());
            doReturn(Optional.of(region)).when(regionRepository).findByName(anyString());
            doReturn("mocked_url").when(s3Service).upload(any(), anyString(), anyString(), anyString());
            doReturn("encoded_password").when(passwordEncoder).encode(anyString());
            doReturn(null).when(memberRepository).save(any(Member.class));
            doReturn("mocked_code").when(mailService).sendVerificationCode(anyString());
            doReturn(null).when(emailVerificationRepository).save(any(EmailVerification.class));

            // when
            SignUpResponse result = memberService.saveMember(signUpRequest, profileImage);

            // then
            assertThat(result.email()).isEqualTo(signUpRequest.email());
        }

        @Test
        @DisplayName("프로필 이미지가 없어도 회원가입에 성공한다.")
        void successWithoutProfileImage() {
            // given
            doReturn(false).when(memberRepository).existsByEmail(anyString());
            doReturn(false).when(memberRepository).existsByNickname(anyString());
            doReturn(Optional.of(region)).when(regionRepository).findByName(anyString());
            doReturn("encoded_password").when(passwordEncoder).encode(anyString());
            doReturn(null).when(memberRepository).save(any(Member.class));
            doReturn("mocked_code").when(mailService).sendVerificationCode(anyString());
            doReturn(null).when(emailVerificationRepository).save(any(EmailVerification.class));

            // when
            SignUpResponse result = memberService.saveMember(signUpRequest, null);

            // then
            assertThat(result.email()).isEqualTo(signUpRequest.email());
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입을 시도하면 예외를 던진다.")
        void duplicateEmail() {
            // given
            doReturn(true).when(memberRepository).existsByEmail(anyString());

            // when & then
            assertThatThrownBy(() -> memberService.saveMember(signUpRequest, profileImage))
              .isInstanceOf(MemberException.class)
              .hasMessageContaining("이미 존재하는 이메일입니다.");
        }

        @Test
        @DisplayName("이미 존재하는 닉네임으로 회원가입을 시도하면 예외를 던진다.")
        void duplicateNickname() {
            // given
            doReturn(false).when(memberRepository).existsByEmail(anyString());
            doReturn(true).when(memberRepository).existsByNickname(anyString());

            // when & then
            assertThatThrownBy(() -> memberService.saveMember(signUpRequest, profileImage))
              .isInstanceOf(MemberException.class)
              .hasMessageContaining("이미 존재하는 닉네임입니다.");
        }

        @Test
        @DisplayName("존재하지 않는 지역으로 회원가입을 시도하면 예외를 던진다.")
        void notFoundRegion() {
            // given
            doReturn(false).when(memberRepository).existsByEmail(anyString());
            doReturn(false).when(memberRepository).existsByNickname(anyString());
            doReturn(Optional.empty()).when(regionRepository).findByName(anyString());

            // when & then
            assertThatThrownBy(() -> memberService.saveMember(signUpRequest, profileImage))
              .isInstanceOf(MemberException.class)
              .hasMessageContaining("존재하지 않는 지역입니다.");
        }
    }

    @Nested
    @DisplayName("updateMember 메서드는")
    class updateMember {

        Region region = new Region("효자동");
        Region updatedRegion = new Region("newRegion");
        Member member = Member.builder()
                              .id(1L)
                              .email("test@naver.com")
                              .nickname("testnick")
                              .password("!1asdasd")
                              .profileImage(
                                "https://matgo-bucket.s3.ap-northeast-2.amazonaws.com/matgo/member/default_image")
                              .role(UserRole.ROLE_USER)
                              .region(region)
                              .isActive(true)
                              .build();

        MultipartFile newProfileImage = new MockMultipartFile("profileImage", "img.jpeg", "image/jpeg",
          "image data".getBytes(StandardCharsets.UTF_8));
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest("newNickname", "newRegion");


        @Test
        @DisplayName("수정에 성공하면 void를 반환한다.")
        void success() {
            // given
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(false).when(memberRepository).existsByNickname(anyString());
            doReturn(Optional.of(updatedRegion)).when(regionRepository).findByName(anyString());
            doReturn("new_mocked_url").when(s3Service).upload(any(), anyString(), anyString(), anyString());

            // when
            memberService.updateMember(member.getId(), memberUpdateRequest, newProfileImage);

            // then
            assertThat(member.getNickname()).isEqualTo(memberUpdateRequest.nickname());
            assertThat(member.getRegion().getName()).isEqualTo(memberUpdateRequest.region());
            assertThat(member.getProfileImage()).isEqualTo("new_mocked_url");
        }

        @Test
        @DisplayName("프로필 이미지가 없어도 수정에 성공한다.")
        void successWithoutProfileImage() {
            // given
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(false).when(memberRepository).existsByNickname(anyString());
            doReturn(Optional.of(updatedRegion)).when(regionRepository).findByName(anyString());

            // when
            memberService.updateMember(member.getId(), memberUpdateRequest, null);

            // then
            assertThat(member.getNickname()).isEqualTo(memberUpdateRequest.nickname());
            assertThat(member.getRegion().getName()).isEqualTo(memberUpdateRequest.region());
            assertThat(member.getProfileImage()).isEqualTo(
              "https://matgo-bucket.s3.ap-northeast-2.amazonaws.com/matgo/member/default_image");
        }

        @Test
        @DisplayName("MemberUpdateRequest에 값이 없으면 update를 하지 않고 기존 값을 유지한다.")
        void updateNothing() {
            // given
            MemberUpdateRequest nullRequest = new MemberUpdateRequest(null, null);
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());

            // when
            memberService.updateMember(member.getId(), nullRequest, null);

            // then
            assertThat(member.getNickname()).isEqualTo("testnick");
            assertThat(member.getRegion().getName()).isEqualTo("효자동");
            assertThat(member.getProfileImage()).isEqualTo(
              "https://matgo-bucket.s3.ap-northeast-2.amazonaws.com/matgo/member/default_image");
        }

        @Test
        @DisplayName("존재하는 닉네임으로 수정을 시도하면 예외를 던진다.")
        void duplicateNickname() {
            // given
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(true).when(memberRepository).existsByNickname(anyString());

            // when & then
            assertThatThrownBy(() -> memberService.updateMember(member.getId(), memberUpdateRequest, null))
              .isInstanceOf(MemberException.class)
              .hasMessageContaining(ALREADY_EXISTED_NICKNAME.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 지역으로 수정을 시도하면 예외를 던진다.")
        void notFoundRegion() {
            // given
            doReturn(Optional.of(member)).when(memberRepository).findById(anyLong());
            doReturn(false).when(memberRepository).existsByNickname(anyString());
            doReturn(Optional.empty()).when(regionRepository).findByName(anyString());

            // when & then
            assertThatThrownBy(() -> memberService.updateMember(member.getId(), memberUpdateRequest, null))
              .isInstanceOf(MemberException.class)
              .hasMessageContaining(NOT_FOUND_REGION.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 회원으로 수정을 시도하면 예외를 던진다.")
        void notFoundMember() {
            // given
            doReturn(Optional.empty()).when(memberRepository).findById(anyLong());

            // when & then
            assertThatThrownBy(() -> memberService.updateMember(member.getId(), memberUpdateRequest, null))
              .isInstanceOf(MemberException.class)
              .hasMessageContaining("존재하지 않는 회원입니다.");
        }
    }
}