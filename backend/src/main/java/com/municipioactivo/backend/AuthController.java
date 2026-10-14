package com.municipioactivo.backend;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/session")
    public ResponseEntity<?> currentSession(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "No hay una sesión iniciada"));
        }

        return usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(authentication.getName())
                .map(usuario -> ResponseEntity.ok(Map.of(
                        "nombre", usuario.getNombre(),
                        "email", usuario.getEmail(),
                        "rol", usuario.getRol(),
                        "privilegios", usuario.getPrivilegios() == null ? "" : usuario.getPrivilegios())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("mensaje", "No hay una sesión iniciada")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        if (request.email() == null || request.password() == null
                || !request.password().matches("[A-Za-z0-9]{6,8}")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Credenciales invalidas"));
        }

        String email = request.email().trim();
        String password = request.password();

        return usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(email)
                .filter(usuario -> passwordEncoder.matches(password, usuario.getPasswordHash()))
                .map(usuario -> {
                    List<GrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toUpperCase()));
                    UsernamePasswordAuthenticationToken authentication =
                            UsernamePasswordAuthenticationToken.authenticated(
                                    usuario.getEmail(), null, authorities);

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                    new HttpSessionSecurityContextRepository().saveContext(context, httpRequest, httpResponse);

                    return ResponseEntity.ok(Map.of(
                            "mensaje", "Login correcto",
                            "nombre", usuario.getNombre(),
                            "email", usuario.getEmail(),
                            "rol", usuario.getRol(),
                            "privilegios", usuario.getPrivilegios()));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("mensaje", "Credenciales invalidas")));
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroRequest request) {
        String email = request.email() == null ? "" : request.email().trim();
        String password = request.password() == null ? "" : request.password();
        String nombre = request.nombre() == null ? "" : request.nombre().trim();

        if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Faltan datos requeridos"));
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Correo inválido"));
        }

        if (!password.matches("[A-Za-z0-9]{6,8}")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "La contraseña debe tener de 6 a 8 letras o números, sin caracteres especiales"));
        }

        email = email.toLowerCase(Locale.ROOT);
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", "El usuario ya existe"));
        }

        String rol = "ciudadano";
        String privilegios = "crear_reclamo,seguimiento_reclamo,ver_novedades";

        try {
            Usuario usuario = new Usuario(email, passwordEncoder.encode(password), nombre, rol, privilegios);
            usuarioRepository.save(usuario);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Usuario creado",
                    "nombre", nombre,
                    "email", email,
                    "rol", rol,
                    "privilegios", privilegios));
        } catch (DataIntegrityViolationException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "El usuario ya existe"));
        }
    }

    public record LoginRequest(String email, String password) {
    }

    public record RegistroRequest(String nombre, String email, String password) {
    }
}
