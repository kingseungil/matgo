package matgo.auth.application;

import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.UNAUTHORIZED;
import static matgo.global.exception.ErrorCode.WRONG_PASSWORD;

import lombok.RequiredArgsConstructor;
import matgo.auth.domain.entity.Token;
import matgo.auth.domain.repository.TokenRepository;
import matgo.auth.dto.request.LoginRequest;
import matgo.auth.dto.request.SendTemporaryPasswordRequest;
import matgo.auth.dto.response.LoginResponse;
import matgo.auth.exception.AuthException;
import matgo.auth.jwt.JwtTokenProvider;
import matgo.auth.security.CustomUserDetailService;
import matgo.global.util.SecurityUtil;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenRepository tokenRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailService customUserDetailService;
    private final TokenService tokenService;
    private final MailService mailService;


    public LoginResponse login(LoginRequest request) {
        UserDetails userDetails = getUserDetails(request);
        checkRole(request.role().name(), userDetails.getAuthorities().iterator().next().getAuthority());
        checkPassword(request.password(), userDetails.getPassword());
        Authentication authentication = SecurityUtil.authenticate(userDetails);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        Token token = new Token(authentication.getName(), accessToken, refreshToken);
        tokenRepository.save(token);

        return new LoginResponse(accessToken);
    }

    private UserDetails getUserDetails(LoginRequest request) {
        return customUserDetailService.loadUserByUsername(request.email());
    }

    private void checkRole(String requestRole, String actualRole) {
        if (!requestRole.equals(actualRole)) {
            throw new AuthException(UNAUTHORIZED);
        }
    }

    private void checkPassword(String password, String encodedPassword) {
        if (!passwordEncoder.matches(password, encodedPassword)) {
            throw new AuthException(WRONG_PASSWORD);
        }
    }

    public void logout(Long userId) {
        tokenService.deleteToken(userId);
    }

    @Transactional
    public void forgetPassword(SendTemporaryPasswordRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                                        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        String temporaryPassword = generateTemporaryPassword();
        String encodedPassword = passwordEncoder.encode(temporaryPassword);
        member.changePassword(encodedPassword);
        mailService.sendTemporaryPassword(request.email(), temporaryPassword);
    }

    private String generateTemporaryPassword() {
        String alphanumeric = RandomStringUtils.randomAlphanumeric(6);
        String digit = RandomStringUtils.randomNumeric(1);
        String special = RandomStringUtils.random(1, "!@#$%^&*()");
        return alphanumeric + digit + special;
    }
}
