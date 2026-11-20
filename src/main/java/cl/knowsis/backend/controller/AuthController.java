package cl.knowsis.backend.controller;

import cl.knowsis.backend.exception.ConflictoException;
import cl.knowsis.backend.model.EstadoUsuario;
import cl.knowsis.backend.model.RefreshToken;
import cl.knowsis.backend.model.Rol;
import cl.knowsis.backend.model.Usuario;
import cl.knowsis.backend.repository.RefreshTokenRepository;
import cl.knowsis.backend.repository.UsuarioRepository;
import cl.knowsis.backend.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login, renovación de sesión y datos del usuario")
public class AuthController {

    //   datos que entran y salen
    public record LoginRequest(@NotBlank String correo, @NotBlank String contrasena) {}

    public record RegistroRequest(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 100) String apellidos,
            @NotBlank @Email @Size(max = 100) String correo,
            @NotBlank @Size(min = 8, max = 72) String contrasena,
            @Size(max = 20) String telefono,
            @Size(max = 80) String region,
            @Size(max = 80) String comuna,
            @Size(max = 300) String direccion) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    /** Usuario que ve el frontend: nunca incluye la contraseña ni su hash. */
    public record UsuarioResponse(Long id, String nombre, String apellidos, String correo, String rol,
                                  String estado, String telefono, String region, String comuna,
                                  String direccion, String rut) {
        static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                    u.getRol().name(), u.getEstado().name(), u.getTelefono(), u.getRegion(),
                    u.getComuna(), u.getDireccion(), u.getRut());
        }
    }

    public record SesionResponse(String accessToken, String refreshToken, String tipo,
                                 long expiraEnSegundos, UsuarioResponse usuario) {}

    //dependencias
    private final UsuarioRepository usuarios;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final long refreshDias;
    private final SecureRandom random = new SecureRandom();

    public AuthController(UsuarioRepository usuarios, RefreshTokenRepository refreshTokens,
                          PasswordEncoder encoder, JwtService jwt,
                          @Value("${app.jwt.refresh-expiration-days}") long refreshDias) {
        this.usuarios = usuarios;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.jwt = jwt;
        this.refreshDias = refreshDias;
    }

    // endpoints

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario (siempre con rol Alumno)")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest d) {
        String correo = d.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictoException("Ya existe una cuenta con ese correo.");
        }
        Usuario u = new Usuario();
        u.setNombre(d.nombre().trim());
        u.setApellidos(d.apellidos().trim());
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(d.contrasena()));
        u.setTelefono(d.telefono());
        u.setRegion(d.region());
        u.setComuna(d.comuna());
        u.setDireccion(d.direccion());
        u.setRol(Rol.Alumno);                     // siempre Alumno, sin importar lo que mande el cliente
        u.setEstado(EstadoUsuario.Activo);
        return UsuarioResponse.de(usuarios.save(u));
    }

    @PostMapping("/login")
    @Transactional
    @Operation(summary = "Inicia sesión: devuelve accessToken, refreshToken y el usuario")
    public SesionResponse login(@Valid @RequestBody LoginRequest d) {
        Usuario u = usuarios.findByCorreoIgnoreCase(d.correo().trim())
                .filter(x -> encoder.matches(d.contrasena(), x.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos."));
        if (u.getEstado() != EstadoUsuario.Activo) {
            throw new DisabledException("Tu cuenta está inactiva.");   // un inactivo no puede entrar
        }
        return crearSesion(u);
    }

    @PostMapping("/refresh")
    @Transactional
    @Operation(summary = "Cambia un refresh válido por un par nuevo (el anterior queda inválido)")
    public SesionResponse refrescar(@Valid @RequestBody RefreshRequest d) {
        RefreshToken guardado = refreshTokens.findByTokenHash(hash(d.refreshToken()))
                .filter(t -> !t.isRevocado() && t.getExpiraEn().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido o vencido."));
        Usuario u = guardado.getUsuario();
        if (u.getEstado() != EstadoUsuario.Activo) {
            throw new DisabledException("Tu cuenta está inactiva.");
        }
        guardado.setRevocado(true);               // el anterior queda inválido
        return crearSesion(u);
    }

    @PostMapping("/logout")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Invalida el refresh token")
    public void logout(@Valid @RequestBody RefreshRequest d) {
        refreshTokens.findByTokenHash(hash(d.refreshToken())).ifPresent(t -> t.setRevocado(true));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Devuelve el usuario del token")
    public UsuarioResponse me(Authentication authentication) {
        Long id = (Long) authentication.getPrincipal();
        return usuarios.findById(id)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new BadCredentialsException("Sesión inválida."));
    }

    //auxiliares

    private SesionResponse crearSesion(Usuario u) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String refreshReal = HexFormat.of().formatHex(bytes);        // esto recibe el cliente

        RefreshToken rt = new RefreshToken();
        rt.setUsuario(u);
        rt.setTokenHash(hash(refreshReal));                          // en la base solo va el hash
        rt.setExpiraEn(LocalDateTime.now().plusDays(refreshDias));   // 7 días
        refreshTokens.save(rt);

        return new SesionResponse(jwt.generarToken(u), refreshReal, "Bearer",
                jwt.getExpiracionSegundos(), UsuarioResponse.de(u));
    }

    private static String hash(String texto) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}