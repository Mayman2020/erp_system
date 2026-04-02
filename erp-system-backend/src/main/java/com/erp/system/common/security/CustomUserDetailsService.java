package com.erp.system.common.security;

import com.erp.system.auth.domain.User;
import com.erp.system.auth.repository.UserRepository;
import com.erp.system.auth.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AccessControlService accessControlService;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        String identifier = usernameOrEmail == null ? "" : usernameOrEmail.trim().toLowerCase();
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("AUTH.ERRORS.INVALID_CREDENTIALS"));

        return new AppUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isActive(),
                accessControlService.authorityCodesFor(user).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList()
        );
    }
}
