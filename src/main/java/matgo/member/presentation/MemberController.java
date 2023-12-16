package matgo.member.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import matgo.member.application.MemberService;
import matgo.member.dto.request.SignUpRequest;
import matgo.member.dto.response.SignUpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/member")
@RequiredArgsConstructor
@RestController
@Validated
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<String> register(
      @ModelAttribute @Valid SignUpRequest signUpRequest
    ) {
        SignUpResponse signUpResponse = memberService.saveMember(signUpRequest);
        return ResponseEntity.created(URI.create("/api/member/" + signUpResponse.id()))
                             .body(signUpResponse.email());
    }
}
