package cl.knowsis.backend.controller;

import cl.knowsis.backend.dto.OrdenRequest;
import cl.knowsis.backend.dto.OrdenResponse;
import cl.knowsis.backend.security.UsuarioActual;
import cl.knowsis.backend.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Órdenes", description = "Compras de cursos: cuotas, pago simulado y cupos")
public class OrdenController {

    private final OrdenService servicio;
    private final UsuarioActual usuarioActual;

    public OrdenController(OrdenService servicio, UsuarioActual usuarioActual) {
        this.servicio = servicio;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Compra cursos",
            description = "El servidor calcula el total. 404 curso inexistente · 409 ya inscrito o sin cupos · "
                    + "400 RUT inválido · 422 pago rechazado. Tarjetas de prueba: 4242 4242 4242 4242 (aprobada) "
                    + "y 4000 0000 0000 0002 (rechazada).")
    public OrdenResponse crear(@Valid @RequestBody OrdenRequest datos) {
        return servicio.crear(usuarioActual.id(), datos);
    }

    @GetMapping("/mias")
    @Operation(summary = "Lista las órdenes del usuario con sesión")
    public List<OrdenResponse> mias() {
        return servicio.misOrdenes(usuarioActual.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una orden por su número (ej. KS-00001)", description = "Solo el dueño o un Admin (si no, 403)")
    public OrdenResponse obtener(@PathVariable("id") String numero) {
        return servicio.obtener(numero, usuarioActual.id(), usuarioActual.esAdmin());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todas las órdenes (solo Admin)")
    public List<OrdenResponse> listar() {
        return servicio.listar();
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Anula una orden y libera sus cupos (solo Admin)")
    public OrdenResponse anular(@PathVariable("id") String numero) {
        return servicio.anular(numero);
    }
}
