package cl.knowsis.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Lee "Authorization: Bearer <token>", lo valida y deja al usuario autenticado. */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7);
            try {
                Claims claims = jwtService.validar(token);
                Long id = Long.valueOf(claims.getSubject());
                String rol = claims.get("rol", String.class);

                var autenticacion = new UsernamePasswordAuthenticationToken(
                        id, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException e) {
                // token inválido o vencido: no se autentica; si la ruta lo exige, responde 401
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}