package matgo.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /*
    시큐리티 인증 관련 예외 발생 시, 해당 메서드 실행
    현재는 resolver에 위임하여 CustomExceptionHandler에서 처리
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) {
        resolver.resolveException(request, response, null, (Exception) request.getAttribute("exception"));
    }
}
