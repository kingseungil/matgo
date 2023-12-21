package matgo.auth.security;

import static matgo.global.exception.ErrorCode.NOT_ACTIVATED_USER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import matgo.auth.exception.AuthException;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return memberRepository.findByEmail(email)
                               .map(this::createUser)
                               .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    private User createUser(Member member) {
        if (!member.isVerified()) {
            throw new AuthException(NOT_ACTIVATED_USER);
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().name());

        return new User(String.valueOf(member.getId()),
          member.getPassword(),
          Collections.singleton(authority));
    }
}
