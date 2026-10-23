package com.municipioactivo.backend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
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
        return new BCryptPasswordEncoder();
    }

    @Bean
    // Define qué endpoints son públicos y cuáles requieren autenticación.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/*.html", "/seguimiento.html", "/Css/**", "/js/**", "/assets/**", "/uploads/**")
                    .permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registro").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/session").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reclamos").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/noticias", "/api/noticias/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/noticias", "/api/noticias/**")
                    .access((authentication, context) -> new AuthorizationDecision(
                            usuarioEsAdministrativo(authentication.get())))
                .requestMatchers(HttpMethod.PUT, "/api/noticias/**")
                    .access((authentication, context) -> new AuthorizationDecision(
                            usuarioEsAdministrativo(authentication.get())))
                .requestMatchers(HttpMethod.DELETE, "/api/noticias/**")
                    .access((authentication, context) -> new AuthorizationDecision(
                            usuarioEsAdministrativo(authentication.get())))
                .requestMatchers(HttpMethod.GET, "/api/reclamos/seguimiento/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reclamos", "/api/reclamos/**")
                    .hasRole("ADMINISTRATIVO")
                .requestMatchers(HttpMethod.PUT, "/api/reclamos/**").hasRole("ADMINISTRATIVO")
                .anyRequest().denyAll());

        return http.build();
    }

    // Check the current database role so an old session authority cannot block a valid administrator.
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
        List<String> allowedOriginPatterns = new ArrayList<>(
            List.of("http://localhost:*", "http://127.0.0.1:*"));
        for (String origin : frontendOrigins.split(",")) {
            if (!origin.isBlank()) {
                allowedOriginPatterns.add(origin.trim());
            }
        }
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
