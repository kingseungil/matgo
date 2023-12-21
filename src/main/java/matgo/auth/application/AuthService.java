package matgo.auth.application;

import static matgo.global.exception.ErrorCode.UNAUTHORIZED;
import static matgo.global.exception.ErrorCode.WRONG_PASSWORD;

import lombok.RequiredArgsConstructor;
import matgo.auth.domain.entity.Token;
import matgo.auth.domain.repository.TokenRepository;
import matgo.auth.dto.request.LoginRequest;
import matgo.auth.dto.response.LoginResponse;
import matgo.auth.exception.AuthException;
import matgo.auth.jwt.JwtTokenProvider;
import matgo.auth.security.CustomUserDetailService;
import matgo.global.util.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailService customUserDetailService;
    private final TokenService tokenService;


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

}
