package matgo.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import matgo.common.BaseServiceTest;
import matgo.member.domain.entity.Member;
import matgo.member.domain.entity.Region;
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
}