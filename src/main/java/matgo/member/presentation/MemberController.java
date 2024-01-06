package matgo.member.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.auth.security.OnlyUser;
import matgo.member.application.MemberService;
import matgo.member.dto.request.MemberUpdateRequest;
import matgo.member.dto.request.ResetPasswordRequest;
import matgo.member.dto.request.SignUpRequest;
import matgo.member.dto.response.MemberResponse;
import matgo.member.dto.response.SignUpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> register(
      @Valid @RequestPart SignUpRequest signUpRequest,
      @RequestPart(required = false) MultipartFile profileImage
    ) {
        SignUpResponse signUpResponse = memberService.saveMember(signUpRequest, profileImage);
        return ResponseEntity.created(URI.create("/api/member/" + signUpResponse.id()))
                             .body(signUpResponse.email());
    }

    // 회원정보 수정
    @PutMapping
    @OnlyUser
    public ResponseEntity<Void> updateMember(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestPart MemberUpdateRequest memberUpdateRequest,
      @RequestPart(required = false) MultipartFile profileImage
    ) {
        memberService.updateMember(Long.parseLong(userDetails.getUsername()), memberUpdateRequest, profileImage);
        return ResponseEntity.noContent().build();
    }

    // 비밀번호 재설정
    @PutMapping("/reset-password")
    @OnlyUser
    public ResponseEntity<Void> resetPassword(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        memberService.resetPassword(Long.parseLong(userDetails.getUsername()), resetPasswordRequest);
        return ResponseEntity.noContent().build();
    }

    // 내 정보 조회
    @GetMapping
    @OnlyUser
    public ResponseEntity<MemberResponse> getMember(
      @AuthenticationPrincipal UserDetails userDetails
    ) {
        MemberResponse memberResponse = memberService.getMember(Long.parseLong(userDetails.getUsername()));
        return ResponseEntity.ok(memberResponse);
    }
}
