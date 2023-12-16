package matgo.auth.presentation;

import lombok.RequiredArgsConstructor;
import matgo.auth.application.MailService;
import matgo.auth.dto.request.EmailVerificationRequest;
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

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmailCode(
      @RequestBody EmailVerificationRequest request) {
        mailService.verifyCode(request);
        return ResponseEntity.ok().build();
    }
}
