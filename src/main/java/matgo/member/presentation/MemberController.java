package matgo.member.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.auth.security.OnlyUser;
import matgo.member.application.MemberService;
import matgo.member.dto.request.SignUpRequest;
import matgo.member.dto.response.SignUpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/member")
@RequiredArgsConstructor
@RestController
@Validated
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<String> register(
      @Valid @RequestPart SignUpRequest signUpRequest,
      @RequestPart(required = false) MultipartFile profileImage
    ) {
        SignUpResponse signUpResponse = memberService.saveMember(signUpRequest, profileImage);
        return ResponseEntity.created(URI.create("/api/member/" + signUpResponse.id()))
                             .body(signUpResponse.email());
    }

    @GetMapping("/me")
    @OnlyUser
    public ResponseEntity<String> getMyInfo(
      @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("userDetails: {}", userDetails.getUsername());
        return ResponseEntity.ok(userDetails.getUsername());
    }

}
