package com.municipioactivo.backend;

import java.nio.charset.StandardCharsets;

final class PasswordPolicy {

    private static final int LEGACY_MIN_LENGTH = 6;
    private static final int NEW_PASSWORD_MIN_LENGTH = 12;
    private static final int BCRYPT_MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    static boolean canAuthenticate(String password) {
        return hasSafeLength(password, LEGACY_MIN_LENGTH);
    }

    static boolean canRegister(String password) {
        return hasSafeLength(password, NEW_PASSWORD_MIN_LENGTH);
    }

    private static boolean hasSafeLength(String password, int minimumLength) {
        if (password == null || password.length() < minimumLength
                || password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            return false;
        }
        return password.chars().noneMatch(Character::isISOControl);
    }
}
