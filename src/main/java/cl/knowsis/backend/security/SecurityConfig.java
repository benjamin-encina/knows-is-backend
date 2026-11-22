package cl.knowsis.backend.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           @Qualifier("corsConfigurationSource") CorsConfigurationSource cors,
                                           JwtService jwtService,
                                           JsonAuthEntryPoint entryPoint,
                                           JsonAccessDeniedHandler accessDenied) throws Exception {
        http
                .csrf(csrf -> csrf.disable())            // API stateless con JWT: no usa cookies de sesión
                .cors(c -> c.configurationSource(cors))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)     // 401 en JSON
                        .accessDeniedHandler(accessDenied))       // 403 en JSON
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()   // /me SÍ exige token
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cursos/**", "/api/categorias").permitAll()
                        .requestMatchers("/api/ping", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Para guardar contraseñas con hash BCrypt (nunca en texto plano). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}