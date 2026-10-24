package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordPolicyTests {

    @Test
    void registrationRequiresTwelveCharactersButLegacyPasswordsCanStillSignIn() {
        assertFalse(PasswordPolicy.canRegister("Clave26"));
        assertTrue(PasswordPolicy.canAuthenticate("Clave26"));
        assertTrue(PasswordPolicy.canRegister("FraseSegura2026!"));
    }

    @Test
    void enforcesBcryptByteLimitAndRejectsControlCharacters() {
        assertFalse(PasswordPolicy.canRegister("ñ".repeat(37)));
        assertFalse(PasswordPolicy.canRegister("Frase\nSegura2026!"));
        assertFalse(PasswordPolicy.canAuthenticate("Clave"));
    }
}
