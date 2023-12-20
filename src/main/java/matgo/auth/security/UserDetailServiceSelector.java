package matgo.auth.security;

import static matgo.global.exception.ErrorCode.NOT_SUPPORTED_USER_TYPE;
import static matgo.member.domain.type.UserRole.ROLE_ADMIN;
import static matgo.member.domain.type.UserRole.ROLE_USER;

import java.util.Map;
import matgo.auth.exception.AuthException;
import matgo.member.domain.type.UserRole;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceSelector {

    private final Map<UserRole, UserDetailsService> userDetailsServiceMap;

    public UserDetailServiceSelector(
      CustomUserDetailService customUserDetailsService,
      AdminDetailService adminDetailService
    ) {
        this.userDetailsServiceMap = Map.of(
          ROLE_USER, customUserDetailsService,
          ROLE_ADMIN, adminDetailService
        );
    }

    /**
     * 유저 타입에 따라 UserDetailsService를 선택
     */
    public UserDetailsService select(UserRole role) {
        UserDetailsService userDetailsService = userDetailsServiceMap.get(role);
        if (userDetailsService == null) {
            throw new AuthException(NOT_SUPPORTED_USER_TYPE);
        }
        return userDetailsService;
    }
}
