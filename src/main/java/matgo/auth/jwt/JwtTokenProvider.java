package matgo.auth.jwt;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static java.nio.charset.StandardCharsets.UTF_8;
import static matgo.global.exception.ErrorCode.EMPTY_ACCESS_TOKEN;
import static matgo.global.exception.ErrorCode.EMPTY_REFRESH_TOKEN;
import static matgo.global.exception.ErrorCode.EXPIRED_ACCESS_TOKEN;
import static matgo.global.exception.ErrorCode.EXPIRED_REFRESH_TOKEN;
import static matgo.global.exception.ErrorCode.INVALID_ACCESS_TOKEN;
import static matgo.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import matgo.auth.application.TokenService;
import matgo.auth.domain.entity.Token;
import matgo.auth.exception.TokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = Duration.ofMinutes(1).toMillis();
    private static final long REFRESH_TOKEN_EXPIRE_TIME = Duration.ofDays(30).toMillis();
    private static final String KEY_ROLE = "role";

    private final SecretKey key;
    private final TokenService tokenService;

    public JwtTokenProvider(
      @Value("${spring.security.jwt.secret}") String key,
      TokenService tokenService
    ) {
        this.key = Keys.hmacShaKeyFor(key.getBytes(UTF_8));
        this.tokenService = tokenService;
    }

    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, ACCESS_TOKEN_EXPIRE_TIME, key);
    }

    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, REFRESH_TOKEN_EXPIRE_TIME, key);
    }

    private String createToken(Authentication authentication, long expireTime, SecretKey secret) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        String authorities = authentication.getAuthorities().stream()
                                           .map(GrantedAuthority::getAuthority)
                                           .collect(Collectors.joining(","));
        claims.put(KEY_ROLE, authorities);
        Date now = new Date();

        return Jwts.builder()
                   .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                   .setClaims(claims)
                   .setIssuedAt(now)
                   .setIssuer("matgo")
                   .setExpiration(new Date(now.getTime() + expireTime))
                   .signWith(secret, HS256)
                   .compact();
    }

    @Transactional
    public String refreshAccessToken(String accessToken) {
        Token token = tokenService.findByAccessToken(accessToken);
        String refreshToken = token.getRefreshToken();
        validateRefreshToken(refreshToken);

        Authentication authentication = getAuthentication(refreshToken);
        return createAccessToken(authentication);
    }


    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                                .setSigningKey(key)
                                .build()
                                .parseClaimsJws(token)
                                .getBody();
            List<SimpleGrantedAuthority> authorities =
              Collections.singletonList(new SimpleGrantedAuthority(claims.get(KEY_ROLE).toString()));

            User principal = new User(claims.getSubject(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        } catch (ExpiredJwtException e) {
            throw new TokenException(EXPIRED_ACCESS_TOKEN);
        }
    }


    public void validateAccessToken(String accessToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken);
        } catch (SecurityException | MalformedJwtException e) {
            throw new TokenException(INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new TokenException(EXPIRED_ACCESS_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new TokenException(EMPTY_ACCESS_TOKEN);
        }
    }

    public void validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
        } catch (SecurityException | MalformedJwtException e) {
            throw new TokenException(INVALID_REFRESH_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new TokenException(EXPIRED_REFRESH_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new TokenException(EMPTY_REFRESH_TOKEN);
        }
    }
}
