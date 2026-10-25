package com.municipioactivo.backend;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
// Configura el cifrado de contraseñas, CORS y las reglas de acceso de la API.
public class SecurityConfig {

    @Value("${FRONTEND_ORIGINS:}")
    private String frontendOrigins;

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Proporciona el codificador BCrypt usado para almacenar y validar contraseñas.
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    // Define qué endpoints son públicos y cuáles requieren autenticación.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                    .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                    .sessionFixation(sessionFixation -> sessionFixation.changeSessionId()))
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/*.html", "/seguimiento.html", "/Css/**", "/js/**", "/assets/**", "/uploads/**")
                    .permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registro").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/session").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reclamos").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/noticias", "/api/noticias/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/noticias", "/api/noticias/**")
                    .access((authentication, context) -> new AuthorizationDecision(
                            usuarioEsAdministrativo(authentication.get())))
                .requestMatchers(HttpMethod.PUT, "/api/noticias", "/api/noticias/**")
                    .access((authentication, context) -> new AuthorizationDecision(
                            usuarioEsAdministrativo(authentication.get())))
                .requestMatchers(HttpMethod.DELETE, "/api/noticias", "/api/noticias/**")
                    .access((authentication, context) -> new AuthorizationDecision(
                            usuarioEsAdministrativo(authentication.get())))
                .requestMatchers(HttpMethod.GET, "/api/reclamos/seguimiento/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reclamos", "/api/reclamos/**")
                    .access((authentication, context) -> new AuthorizationDecision(
                            usuarioEsAdministrativo(authentication.get())))
                .requestMatchers(HttpMethod.PUT, "/api/reclamos", "/api/reclamos/**")
                    .access((authentication, context) -> new AuthorizationDecision(
                            usuarioEsAdministrativo(authentication.get())))
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .anyRequest().denyAll());

        return http.build();
    }

    // Valida el rol tanto en authorities de sesión en memoria como en base de datos.
    private boolean usuarioEsAdministrativo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }

        return usuarioRepository.findByEmailIgnoreCaseAndActivoTrue(authentication.getName())
                .map(usuario -> "administrativo".equalsIgnoreCase(usuario.getRol()))
                .orElse(false);
    }

    @Bean
    // Permite llamadas del frontend local y de los orígenes de despliegue configurados.
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> allowedOrigins = Arrays.stream(frontendOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Content-Type", "Accept", "Authorization", "X-Requested-With", "X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
