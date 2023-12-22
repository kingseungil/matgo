package matgo.auth.application;

import static matgo.global.exception.ErrorCode.EXPIRED_REFRESH_TOKEN;

import lombok.RequiredArgsConstructor;
import matgo.auth.domain.entity.Token;
import matgo.auth.domain.repository.TokenRepository;
import matgo.auth.exception.TokenException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public void deleteToken(Long id) {
        tokenRepository.deleteById(id);
    }

    public Token findByAccessToken(String accessToken) {
        System.out.println("accessToken = " + accessToken);
        return tokenRepository.findByAccessToken(accessToken)
                              .orElseThrow(() -> new TokenException(EXPIRED_REFRESH_TOKEN));
    }

    @Deprecated
    public void saveAccessToken(Token token) {
        tokenRepository.save(token);
    }
}
