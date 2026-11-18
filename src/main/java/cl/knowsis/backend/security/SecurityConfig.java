package cl.knowsis.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * ===================== CONFIGURACIÓN TEMPORAL =====================
 * Permite TODAS las peticiones para que los tres frentes puedan probar desde el día 1.
 * El frente B (autenticación) la reemplaza por la real:
 *   - rutas públicas: /api/auth/**, GET /api/cursos/**, GET /api/categorias, /swagger-ui/**, /v3/api-docs/**
 *   - todo lo demás exige un JWT válido (JwtAuthFilter antes de UsernamePasswordAuthenticationFilter)
 *   - respuestas JSON para 401 (sin token o inválido) y 403 (sin permiso)
 *
 * Mientras tanto NO uses @PreAuthorize: sin sesión siempre responderá 403.
 * ==================================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        http
                .csrf(csrf -> csrf.disable())            // API stateless con JWT: no usa cookies de sesión
                .cors(c -> c.configurationSource(cors))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll());   // TEMPORAL
        return http.build();
    }

    /** Para guardar contraseñas con hash BCrypt (nunca en texto plano). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
