package matgo.auth.jwt;

import static matgo.global.exception.ErrorCode.EXPIRED_ACCESS_TOKEN;
import static matgo.global.exception.ErrorCode.EXPIRED_REFRESH_TOKEN;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.auth.exception.TokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final String[] WHITELIST = {
      "/docs/**", // swagger
      "/v3/api-docs/**", // swagger
      "/api/auth/login", // 로그인
      "/api/auth/verify-emailcode", // 이메일 인증
      "/api/member/signup",     // 회원가입
      "/api/auth/send-temporary-password", // 임시 비밀번호 발급
      "/api/restaurants", // 전체 식당 목록 조회
      "/api/restaurants/address", // 주소로 식당 목록 조회
      "/api/restaurants/search", // 식당 검색
      "/api/restaurants/detail/**", // 식당 상세 조회
      "/api/reviews/detail/{reviewId}", // 리뷰 상세 조회
      "/api/reviews/{restaurantId}", // 식당 리뷰 목록 조회
    };
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

        String path = request.getRequestURI();

        if (Arrays.stream(WHITELIST).anyMatch(pattern -> antPathMatcher.match(pattern, path))) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveToken(request);
        try {
            // Access Token 유효성 검사
            jwtTokenProvider.validateAccessToken(accessToken);
            Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (TokenException e) {
            // Access Token 만료 시 Refresh Token 유효성 검사
            if (e.getErrorCode().equals(EXPIRED_ACCESS_TOKEN)) {
                try {
                    String newAccessToken = jwtTokenProvider.refreshAccessToken(accessToken);
                    Authentication authentication = jwtTokenProvider.getAuthentication(newAccessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    response.setHeader(AUTHORIZATION, BEARER + newAccessToken);
                } catch (TokenException ex) {
                    // Refresh Token 만료 시
                    if (ex.getErrorCode().equals(EXPIRED_REFRESH_TOKEN)) {
                        log.error("Refresh token has expired");
                        request.setAttribute("exception", ex);
                    } else {
                        log.error("TokenException: {}", ex.getMessage());
                        request.setAttribute("exception", ex);
                    }
                }
            } else {
                log.error("TokenException: {}", e.getMessage());
                request.setAttribute("exception", e);
            }
        }

        filterChain.doFilter(request, response);

    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(BEARER.length());
        }

        return null;
    }
}
