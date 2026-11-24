package cl.knowsis.backend.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Forma simple de saber quién hace la petición desde un servicio o controlador.
 * JwtAuthFilter deja como principal el id del usuario (Long) y la autoridad ROLE_<ROL>.
 *
 * (Lo pide el frente C; cuando el frente B entregue su propio usuarioActual() se puede reemplazar.)
 */
@Component
public class UsuarioActual {

    /** Id del usuario con sesión. Sin sesión lanza una AuthenticationException (→ 401). */
    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long id)) {
            throw new AuthenticationCredentialsNotFoundException("No hay una sesión iniciada");
        }
        return id;
    }

    public boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
