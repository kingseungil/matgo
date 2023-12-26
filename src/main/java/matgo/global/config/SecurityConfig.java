package matgo.global.config;

import lombok.RequiredArgsConstructor;
import matgo.auth.jwt.JwtTokenFilter;
import matgo.auth.security.CustomAccessDeniedHandler;
import matgo.auth.security.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity(debug = true)
@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return webSecurity -> webSecurity.ignoring().requestMatchers("/docs/**", "/error", "/v3/api-docs/**");
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(AbstractHttpConfigurer::disable)
          .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(customAuthenticationEntryPoint)
            .accessDeniedHandler(customAccessDeniedHandler)
          )
          .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(authorizeRequests -> authorizeRequests
            // 모두 허용
            .requestMatchers(
              "/api/member/signup", // 회원가입
              "/api/auth/verify-emailcode", // 이메일 인증 코드 확인
              "/api/auth/login", // 로그인
              "/api/auth/send-temporary-password", // 임시 비밀번호 발급
              "/api/restaurants", // 전체 식당 목록 조회
              "/api/restaurants/address" // 주소로 식당 목록 조회
            ).permitAll()
            // 고객만 허용
            .requestMatchers(HttpMethod.PUT, "/api/member").hasRole("USER") // 회원 정보 수정
            .requestMatchers(HttpMethod.PUT, "/api/member/reset-password").hasRole("USER") // 비밀번호 재설정
            .requestMatchers(HttpMethod.GET, "/api/restaurants/nearby").hasRole("USER") // 내 주변 식당 목록 조회
            // 관리자만 허용
            // 그 외는 인증 필요
            .anyRequest().authenticated())
          // jwt filter 추가
          .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
