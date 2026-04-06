package com.erp.system.auth.util;

import java.util.Locale;

public final class UserProfileI18n {

    private UserProfileI18n() {
    }

    public static String resolveFullName(String fullNameEn, String fullNameAr, Locale locale) {
        boolean preferAr = locale != null && "ar".equalsIgnoreCase(locale.getLanguage());
        if (preferAr) {
            if (notBlank(fullNameAr)) {
                return fullNameAr.trim();
            }
            if (notBlank(fullNameEn)) {
                return fullNameEn.trim();
            }
        } else {
            if (notBlank(fullNameEn)) {
                return fullNameEn.trim();
            }
            if (notBlank(fullNameAr)) {
                return fullNameAr.trim();
            }
        }
        return "";
    }

    public static String resolveCompanyName(String companyNameEn, String companyNameAr, Locale locale) {
        boolean preferAr = locale != null && "ar".equalsIgnoreCase(locale.getLanguage());
        if (preferAr) {
            if (notBlank(companyNameAr)) {
                return companyNameAr.trim();
            }
            if (notBlank(companyNameEn)) {
                return companyNameEn.trim();
            }
        } else {
            if (notBlank(companyNameEn)) {
                return companyNameEn.trim();
            }
            if (notBlank(companyNameAr)) {
                return companyNameAr.trim();
            }
        }
        return "";
    }

    /**
     * Keeps {@code full_name} populated for legacy queries (prefers English).
     */
    public static String syncLegacyFullName(String fullNameEn, String fullNameAr) {
        if (notBlank(fullNameEn)) {
            return fullNameEn.trim();
        }
        if (notBlank(fullNameAr)) {
            return fullNameAr.trim();
        }
        return "";
    }

    public static String syncLegacyCompanyName(String companyNameEn, String companyNameAr) {
        if (notBlank(companyNameEn)) {
            return companyNameEn.trim();
        }
        if (notBlank(companyNameAr)) {
            return companyNameAr.trim();
        }
        return null;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
