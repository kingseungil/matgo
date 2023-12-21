package matgo.auth.application;

import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.UNAUTHORIZED;
import static matgo.global.exception.ErrorCode.WRONG_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import matgo.auth.domain.entity.Token;
import matgo.auth.domain.repository.TokenRepository;
import matgo.auth.dto.request.LoginRequest;
import matgo.auth.dto.response.LoginResponse;
import matgo.auth.exception.AuthException;
import matgo.auth.jwt.JwtTokenProvider;
import matgo.auth.security.CustomUserDetailService;
import matgo.common.BaseServiceTest;
import matgo.global.util.SecurityUtil;
import matgo.member.domain.type.UserRole;
import matgo.member.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class AuthServiceTest extends BaseServiceTest {

    @InjectMocks
    private AuthService authService;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private CustomUserDetailService customUserDetailService;
    @Mock
    private TokenService tokenService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserDetails userDetails;

    @Nested
    @DisplayName("login 메서드는")
    class login {

        LoginRequest loginRequest = new LoginRequest("test@naver.com", "1!asdasd", UserRole.ROLE_USER);

        @Test
        @DisplayName("로그인에 성공하면 토큰을 반환한다.")
        void success() {
            // given
            doReturn(userDetails).when(customUserDetailService).loadUserByUsername(any());
            GrantedAuthority authority = new SimpleGrantedAuthority(UserRole.ROLE_USER.name());
            doReturn(Collections.singleton(authority)).when(userDetails).getAuthorities();
            Authentication authentication = SecurityUtil.authenticate(userDetails);
            doReturn("encodedPassword").when(userDetails).getPassword();
            doReturn(true).when(passwordEncoder).matches(any(), any());
            doReturn("accessToken").when(jwtTokenProvider).createAccessToken(authentication);
            doReturn("refreshToken").when(jwtTokenProvider).createRefreshToken(authentication);
            doReturn(new Token("test@naver.com", "accessToken", "refreshToken")).when(tokenRepository).save(any());

            // when
            LoginResponse result = authService.login(loginRequest);

            // then
            assertThat(result.accessToken()).isEqualTo("accessToken");
        }

        @Test
        @DisplayName("비밀번호가 틀리면 예외를 던진다.")
        void wrongPassword() {
            // given
            GrantedAuthority authority = new SimpleGrantedAuthority(UserRole.ROLE_USER.name());
            doReturn(Collections.singleton(authority)).when(userDetails).getAuthorities();
            doReturn(userDetails).when(customUserDetailService).loadUserByUsername(any());
            doReturn("encodedPassword").when(userDetails).getPassword();

            // when & then
            assertThatThrownBy(() -> authService.login(loginRequest))
              .isInstanceOf(AuthException.class)
              .hasMessageContaining(WRONG_PASSWORD.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인을 시도하면 예외를 던진다.")
        void notExistedEmail() {
            // given
            doThrow(new MemberException(NOT_FOUND_MEMBER)).when(customUserDetailService).loadUserByUsername(any());

            // when & then
            assertThatThrownBy(() -> authService.login(loginRequest))
              .isInstanceOf(MemberException.class)
              .hasMessageContaining(NOT_FOUND_MEMBER.getMessage());
        }

        @Test
        @DisplayName("권한이 없는 사용자가 로그인을 시도하면 예외를 던진다.")
        void notAuthorizedUser() {
            // given
            GrantedAuthority authority = new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name());
            doReturn(Collections.singleton(authority)).when(userDetails).getAuthorities();
            doReturn(userDetails).when(customUserDetailService).loadUserByUsername(any());

            // when & then
            assertThatThrownBy(() -> authService.login(loginRequest))
              .isInstanceOf(AuthException.class)
              .hasMessageContaining(UNAUTHORIZED.getMessage());
        }
    }

    @Nested
    @DisplayName("logout 메서드는")
    class logout {

        @Test
        @DisplayName("로그아웃에 성공하면 토큰을 삭제한다.")
        void success() {
            // given
            Long userId = 1L;

            // when
            authService.logout(userId);

            // then
            verify(tokenService).deleteToken(userId);
        }
    }
}