package matgo.global.util;

import lombok.extern.slf4j.Slf4j;
import matgo.member.domain.type.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public class SecurityUtil {


    public static Authentication authenticate(UserDetails userDetails) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    public static boolean hasRole(UserRole role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role.name()));
    }

}
