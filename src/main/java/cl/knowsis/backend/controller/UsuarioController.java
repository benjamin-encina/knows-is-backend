package cl.knowsis.backend.controller;

import cl.knowsis.backend.controller.AuthController.UsuarioResponse;
import cl.knowsis.backend.exception.ConflictoException;
import cl.knowsis.backend.exception.RecursoNoEncontradoException;
import cl.knowsis.backend.exception.ReglaNegocioException;
import cl.knowsis.backend.model.EstadoUsuario;
import cl.knowsis.backend.model.Rol;
import cl.knowsis.backend.model.Usuario;
import cl.knowsis.backend.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")                      // TODO el controller es solo para Admin
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Administración de usuarios (solo Admin)")
public class UsuarioController {

    /** Cuerpo de POST y PUT. En PUT, "contrasena" vacía o ausente significa "no cambiarla". */
    public record UsuarioRequest(
            @NotBlank @Size(max = 100) String nombre,
            @Size(max = 100) String apellidos,
            @NotBlank @Email @Size(max = 100) String correo,
            String contrasena,
            @Size(max = 20) String telefono,
            @Size(max = 80) String region,
            @Size(max = 80) String comuna,
            @Size(max = 300) String direccion,
            @Size(max = 12) String rut,
            @NotNull Rol rol,
            EstadoUsuario estado) {}

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @GetMapping
    @Operation(summary = "Lista todos los usuarios")
    public List<UsuarioResponse> listar() {
        return usuarios.findAll(Sort.by("id")).stream().map(UsuarioResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por id")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario con el rol indicado")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest d) {
        String correo = d.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictoException("Ya existe un usuario con ese correo.");
        }
        if (d.contrasena() == null || d.contrasena().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria.");
        }
        validarContrasena(d.contrasena());

        Usuario u = new Usuario();
        u.setPasswordHash(encoder.encode(d.contrasena()));
        copiarDatos(u, d, correo);
        return UsuarioResponse.de(usuarios.save(u));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita un usuario (contraseña vacía = no cambiarla)")
    public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest d) {
        Usuario u = buscar(id);
        String correo = d.correo().trim().toLowerCase();

        // el correo no puede pertenecer a OTRO usuario
        usuarios.findByCorreoIgnoreCase(correo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> { throw new ConflictoException("Ya existe un usuario con ese correo."); });

        if (d.contrasena() != null && !d.contrasena().isBlank()) {
            validarContrasena(d.contrasena());
            u.setPasswordHash(encoder.encode(d.contrasena()));
        }
        copiarDatos(u, d, correo);
        return UsuarioResponse.de(usuarios.save(u));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario (un admin no puede eliminarse a sí mismo)")
    public void eliminar(@PathVariable Long id, Authentication authentication) {
        if (id.equals(authentication.getPrincipal())) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta.");
        }
        usuarios.delete(buscar(id));
    }

    // ---------------------------------------------------------------- auxiliares

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("Usuario", id));
    }

    private static void validarContrasena(String contrasena) {
        if (contrasena.length() < 8 || contrasena.length() > 72) {
            throw new ReglaNegocioException("La contraseña debe tener entre 8 y 72 caracteres.");
        }
    }

    private static void copiarDatos(Usuario u, UsuarioRequest d, String correo) {
        u.setNombre(d.nombre().trim());
        u.setApellidos(vacioANulo(d.apellidos()));
        u.setCorreo(correo);
        u.setTelefono(vacioANulo(d.telefono()));
        u.setRegion(vacioANulo(d.region()));
        u.setComuna(vacioANulo(d.comuna()));
        u.setDireccion(vacioANulo(d.direccion()));
        u.setRut(vacioANulo(d.rut()));
        u.setRol(d.rol());
        u.setEstado(d.estado() != null ? d.estado() : EstadoUsuario.Activo);
    }

    private static String vacioANulo(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}