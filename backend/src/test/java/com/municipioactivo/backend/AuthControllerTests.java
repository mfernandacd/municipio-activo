package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthControllerTests {

    @Test
    void currentSessionReturnsTheAuthenticatedUser() {
        UsuarioRepository usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        AuthController controller = new AuthController(usuarioRepository, passwordEncoder);
        Usuario usuario = new Usuario("usuario@example.com", "hash", "Usuario", "ciudadano", "crear_reclamo");
        when(usuarioRepository.findByEmailIgnoreCaseAndActivoTrue("usuario@example.com"))
                .thenReturn(Optional.of(usuario));
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "usuario@example.com", null, java.util.List.of());

        var response = controller.currentSession(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("usuario@example.com", ((java.util.Map<?, ?>) response.getBody()).get("email"));
    }

    @Test
    void currentSessionRejectsAnonymousRequests() {
        UsuarioRepository usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        AuthController controller = new AuthController(usuarioRepository, passwordEncoder);

        var response = controller.currentSession(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        org.mockito.Mockito.verifyNoInteractions(usuarioRepository);
    }

    @Test
    void logoutInvalidatesTheSessionAndClearsTheSecurityContext() {
        UsuarioRepository usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        AuthController controller = new AuthController(usuarioRepository, passwordEncoder);
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("usuario@example.com", null, java.util.List.of()));

        var response = controller.logout(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(session).invalidate();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrationNeverAssignsAdministrativeRoleBasedOnEmailDomain() {
        UsuarioRepository usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        AuthController controller = new AuthController(usuarioRepository, passwordEncoder);
        when(usuarioRepository.findByEmailIgnoreCase("empleado@serranoble.gob.ar")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Clave26")).thenReturn("bcrypt-hash");

        var response = controller.registrarUsuario(
                new AuthController.RegistroRequest("Empleado Municipal", "empleado@serranoble.gob.ar", "Clave26"));

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ciudadano", usuarioCaptor.getValue().getRol());
        assertEquals("empleado@serranoble.gob.ar", usuarioCaptor.getValue().getEmail());
    }

    @Test
    void registrationRejectsPasswordsOutsideTheSixToEightCharacterAlphanumericRule() {
        UsuarioRepository usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        AuthController controller = new AuthController(usuarioRepository, passwordEncoder);

        for (String password : new String[]{"Ab123", "Ab12!6", "Ab1234567"}) {
            var response = controller.registrarUsuario(new AuthController.RegistroRequest(
                    "Usuario", "usuario@example.com", password));

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        org.mockito.Mockito.verify(usuarioRepository, org.mockito.Mockito.never()).save(any());
    }
}
