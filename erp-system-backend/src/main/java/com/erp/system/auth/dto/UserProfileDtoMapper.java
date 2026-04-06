package com.erp.system.auth.dto;

import com.erp.system.auth.domain.UserProfile;
import com.erp.system.auth.util.UserProfileI18n;

import java.util.Locale;

public final class UserProfileDtoMapper {

    private UserProfileDtoMapper() {
    }

    public static UserProfileDto from(UserProfile profile, long userId, Locale locale) {
        if (profile == null) {
            return null;
        }
        Locale effective = locale != null ? locale : Locale.ENGLISH;
        return UserProfileDto.builder()
                .id(profile.getId())
                .userId(userId)
                .fullName(UserProfileI18n.resolveFullName(profile.getFullNameEn(), profile.getFullNameAr(), effective))
                .fullNameEn(profile.getFullNameEn())
                .fullNameAr(profile.getFullNameAr())
                .profileImage(profile.getProfileImage())
                .nationalId(profile.getNationalId())
                .companyName(UserProfileI18n.resolveCompanyName(profile.getCompanyNameEn(), profile.getCompanyNameAr(), effective))
                .companyNameEn(profile.getCompanyNameEn())
                .companyNameAr(profile.getCompanyNameAr())
                .build();
    }
}
