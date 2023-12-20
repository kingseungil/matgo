package matgo.auth.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matgo.auth.application.AuthService;
import matgo.auth.application.MailService;
import matgo.auth.dto.request.EmailVerificationRequest;
import matgo.auth.dto.request.LoginRequest;
import matgo.auth.dto.response.LoginResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MailService mailService;
    private final AuthService authService;

    @PostMapping("/verify-emailcode")
    public ResponseEntity<Void> verifyEmailCode(
      @RequestBody EmailVerificationRequest request) {
        mailService.verifyCode(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.signIn(request);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + response.accessToken());
        return ResponseEntity.ok().headers(httpHeaders).body(response);
    }

}
