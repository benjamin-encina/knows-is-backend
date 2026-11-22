package cl.knowsis.backend.security;

import cl.knowsis.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.access-expiration-minutes}") long minutos) {
        // el secreto viene de JWT_SECRET (mínimo 32 caracteres para HS256)
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = minutos * 60 * 1000;
    }

    /** Genera el token de acceso: lleva id (subject), correo y rol, y vence a los 15 minutos. */
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("correo", usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /** Valida firma y vencimiento. Si algo falla lanza JwtException. */
    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }
}