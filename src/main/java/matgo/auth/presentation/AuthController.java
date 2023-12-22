package matgo.auth.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matgo.auth.application.AuthService;
import matgo.auth.application.MailService;
import matgo.auth.dto.request.EmailVerificationRequest;
import matgo.auth.dto.request.LoginRequest;
import matgo.auth.dto.request.SendTemporaryPasswordRequest;
import matgo.auth.dto.response.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
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
      @Valid @RequestBody EmailVerificationRequest request) {
        mailService.verifyCode(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
      @AuthenticationPrincipal UserDetails userDetails
    ) {
        authService.logout(Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-temporary-password")
    public ResponseEntity<Void> sendTemporaryPassword(
      @Valid @RequestBody SendTemporaryPasswordRequest request
    ) {
        authService.forgetPassword(request);
        return ResponseEntity.noContent().build();
    }

}
