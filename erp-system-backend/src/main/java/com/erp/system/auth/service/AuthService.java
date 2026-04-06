package com.erp.system.auth.service;

import com.erp.system.auth.domain.RegistrationType;
import com.erp.system.auth.domain.PasswordResetOtp;
import com.erp.system.auth.domain.User;
import com.erp.system.auth.domain.UserProfile;
import com.erp.system.auth.domain.UserRole;
import com.erp.system.auth.dto.AuthLoginRequestDto;
import com.erp.system.auth.dto.AuthLoginRolesRequestDto;
import com.erp.system.auth.dto.AuthRegisterRequestDto;
import com.erp.system.auth.dto.AuthResponseDto;
import com.erp.system.auth.dto.AuthUserDto;
import com.erp.system.auth.dto.PasswordResetOtpConfirmRequestDto;
import com.erp.system.auth.dto.PasswordResetOtpSendRequestDto;
import com.erp.system.auth.dto.UserProfileDtoMapper;
import com.erp.system.auth.repository.PasswordResetOtpRepository;
import com.erp.system.auth.repository.UserRepository;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.security.AppUserPrincipal;
import com.erp.system.common.security.JwtProperties;
import com.erp.system.auth.util.UserProfileI18n;
import com.erp.system.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessControlService accessControlService;

    @Transactional(readOnly = true)
    public AuthResponseDto login(AuthLoginRequestDto request) {
        String identifier = normalizeIdentifier(request.getUsernameOrEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
            );
            AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
            User user = getActiveUser(principal.getUserId());
            return buildResponse(user);
        } catch (DisabledException ex) {
            throw new BusinessException("AUTH.ERRORS.ACCOUNT_DISABLED");
        } catch (BadCredentialsException ex) {
            throw new BusinessException("AUTH.ERRORS.INVALID_CREDENTIALS");
        } catch (AuthenticationException ex) {
            throw new BusinessException("AUTH.ERRORS.INVALID_CREDENTIALS");
        }
    }

    @Transactional(readOnly = true)
    public List<String> resolveLoginRoles(AuthLoginRolesRequestDto request) {
        String identifier = normalizeIdentifier(request.getUsernameOrEmail());
        return userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
                .map(accessControlService::authorityCodesFor)
                .orElse(List.of());
    }

    @Transactional
    public boolean sendPasswordResetOtp(PasswordResetOtpSendRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !user.isActive()) {
            return true;
        }
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        passwordResetOtpRepository.save(PasswordResetOtp.builder()
                .email(email)
                .otpCode(code)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .used(false)
                .build());
        return true;
    }

    @Transactional
    public boolean resetPasswordWithOtp(PasswordResetOtpConfirmRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        PasswordResetOtp otp = passwordResetOtpRepository.findFirstByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessException("AUTH.ERRORS.INVALID_OTP"));
        if (otp.getExpiresAt().isBefore(Instant.now()) || !otp.getOtpCode().equals(request.getOtpCode().trim())) {
            throw new BusinessException("AUTH.ERRORS.INVALID_OTP");
        }
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("AUTH.ERRORS.INVALID_REQUEST"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        otp.setUsed(true);
        passwordResetOtpRepository.save(otp);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public AuthResponseDto register(AuthRegisterRequestDto request) {
        String username = normalizeIdentifier(request.getUsername());
        String email = normalizeEmail(request.getEmail());
        String phone = normalizePhone(request.getPhone());
        String fullNameEn = coalesceNonBlank(request.getFullNameEn(), request.getFullName());
        String fullNameAr = coalesceNonBlank(request.getFullNameAr(), request.getFullName());
        if (fullNameEn.isBlank() || fullNameAr.isBlank()) {
            throw new BusinessException("AUTH.ERRORS.INVALID_REQUEST");
        }

        ensureUserDoesNotExist(username, email);

        User user = User.builder()
                .username(username)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ACCOUNTANT)
                .active(true)
                .build();

        user.setProfile(buildProfileForRegister(request, fullNameEn, fullNameAr));

        return buildResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponseDto refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank() || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("AUTH.ERRORS.INVALID_REFRESH_TOKEN");
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException("AUTH.ERRORS.INVALID_REFRESH_TOKEN");
        }

        Long userId = jwtTokenProvider.getClaims(refreshToken).userId();
        User user = getActiveUser(userId);
        return buildResponse(user);
    }

    private AuthResponseDto buildResponse(User user) {
        List<String> roles = accessControlService.authorityCodesFor(user);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), roles);

        return AuthResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("ACCESS")
                .roles(roles)
                .userType(user.getRole().name())
                .displayName(resolveDisplayName(user))
                .expiresInSeconds(jwtProperties.getExpirationMs() / 1000)
                .refreshExpiresInSeconds(jwtProperties.getRefreshExpirationMs() / 1000)
                .user(toUserDto(user))
                .build();
    }

    private AuthUserDto toUserDto(User user) {
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
                .profile(UserProfileDtoMapper.from(profile, user.getId(), LocaleContextHolder.getLocale()))
                .build();
    }

    private User getActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("AUTH.ERRORS.ACCOUNT_DISABLED"));
    }

    private void ensureUserDoesNotExist(String username, String email) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("AUTH.ERRORS.USERNAME_IN_USE");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("AUTH.ERRORS.EMAIL_IN_USE");
        }
    }

    private String resolveDisplayName(User user) {
        UserProfile profile = user.getProfile();
        if (profile != null) {
            String localized = UserProfileI18n.resolveFullName(
                    profile.getFullNameEn(), profile.getFullNameAr(), LocaleContextHolder.getLocale());
            if (!localized.isBlank()) {
                return localized;
            }
        }
        return user.getUsername();
    }

    private UserProfile buildProfileForRegister(AuthRegisterRequestDto request, String fullNameEn, String fullNameAr) {
        String companyNameEn;
        String companyNameAr;
        if (request.getRegistrationType() == RegistrationType.COMPANY) {
            companyNameEn = coalesceNonBlank(request.getCompanyNameEn(), request.getCompanyName(), fullNameEn);
            companyNameAr = coalesceNonBlank(request.getCompanyNameAr(), request.getCompanyName(), fullNameAr);
        } else {
            String en = coalesceNonBlank(request.getCompanyNameEn(), request.getCompanyName());
            String ar = coalesceNonBlank(request.getCompanyNameAr(), request.getCompanyName());
            companyNameEn = en.isBlank() ? null : en;
            companyNameAr = ar.isBlank() ? null : ar;
        }
        return UserProfile.builder()
                .fullNameEn(fullNameEn)
                .fullNameAr(fullNameAr)
                .fullName(UserProfileI18n.syncLegacyFullName(fullNameEn, fullNameAr))
                .nationalId(normalizeOptional(request.getNationalId()))
                .companyNameEn(companyNameEn)
                .companyNameAr(companyNameAr)
                .companyName(UserProfileI18n.syncLegacyCompanyName(companyNameEn, companyNameAr))
                .build();
    }

    private String coalesceNonBlank(String... parts) {
        if (parts == null) {
            return "";
        }
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                return part.trim();
            }
        }
        return "";
    }

    private String normalizeIdentifier(String value) {
        return normalizeRequired(value).toLowerCase();
    }

    private String normalizeEmail(String value) {
        return normalizeRequired(value).toLowerCase();
    }

    private String normalizePhone(String value) {
        return normalizeRequired(value);
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
