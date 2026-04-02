package com.erp.system.auth.service;

import com.erp.system.auth.domain.User;
import com.erp.system.auth.domain.UserProfile;
import com.erp.system.auth.dto.AuthUserDto;
import com.erp.system.auth.dto.UpdateProfileRequestDto;
import com.erp.system.auth.dto.UserProfileDto;
import com.erp.system.auth.repository.UserProfileRepository;
import com.erp.system.auth.repository.UserRepository;
import com.erp.system.auth.service.AccessControlService;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.security.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AccessControlService accessControlService;

    @Transactional(readOnly = true)
    public AuthUserDto getMyProfile() {
        return toDto(getCurrentUser());
    }

    @Transactional
    public AuthUserDto updateMyProfile(UpdateProfileRequestDto request) {
        User user = getCurrentUser();

        String username = normalizeRequired(request.getUsername()).toLowerCase();
        String email = normalizeRequired(request.getEmail()).toLowerCase();
        String phone = normalizeRequired(request.getPhone());

        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, user.getId())) {
            throw new BusinessException("PROFILE.ERRORS.USERNAME_IN_USE");
        }
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, user.getId())) {
            throw new BusinessException("PROFILE.ERRORS.EMAIL_IN_USE");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
            user.setProfile(profile);
        }

        profile.setFullName(normalizeRequired(request.getFullName()));
        profile.setProfileImage(normalizeOptional(request.getProfileImage()));
        profile.setNationalId(normalizeOptional(request.getNationalId()));
        profile.setCompanyName(normalizeOptional(request.getCompanyName()));

        userProfileRepository.save(profile);
        return toDto(userRepository.save(user));
    }

    private AuthUserDto toDto(User user) {
        UserProfile profile = user.getProfile();
        return AuthUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .roles(accessControlService.authorityCodesFor(user))
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .profile(profile == null ? null : UserProfileDto.builder()
                        .id(profile.getId())
                        .userId(user.getId())
                        .fullName(profile.getFullName())
                        .profileImage(profile.getProfileImage())
                        .nationalId(profile.getNationalId())
                        .companyName(profile.getCompanyName())
                        .build())
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new BusinessException("AUTH.ERRORS.UNAUTHORIZED");
        }
        return userRepository.findById(principal.userId())
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("AUTH.ERRORS.UNAUTHORIZED"));
    }

    private String normalizeRequired(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new BusinessException("AUTH.ERRORS.INVALID_REQUEST");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        String normalized = value == null ? null : value.trim();
        return normalized == null || normalized.isBlank() ? null : normalized;
    }
}
