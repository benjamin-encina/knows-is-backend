package cl.knowsis.backend.controller;

import cl.knowsis.backend.dto.CursoRequest;
import cl.knowsis.backend.dto.CursoResponse;
import cl.knowsis.backend.model.Nivel;
import cl.knowsis.backend.service.CursoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos")

public class CursoController {

    private final CursoService servicio;

    public CursoController(CursoService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Lista los cursos", description = "Filtros opcionales: categoria, nivel y q (texto en nombre o descripción).")
    @GetMapping
    public List<CursoResponse> listar(@RequestParam(required = false) String categoria,
                                      @RequestParam(required = false) Nivel nivel,
                                      @RequestParam(required = false) String q) {
        return servicio.listar(categoria, nivel, q);
    }

    @Operation(summary = "Obtiene un curso por id", description = "Incluye cuposOcupados y cuposDisponibles. 404 si no existe.")
    @GetMapping("/{id}")
    public CursoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crea un curso (solo Admin)",
            description = "201 si se crea. 400 si faltan datos, 409 si el código ya existe, 422 si rompe una regla de negocio. 403 si no eres Admin.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CursoResponse crear(@Valid @RequestBody CursoRequest datos) {
        return servicio.crear(datos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Edita un curso (solo Admin)",
            description = "200 si se edita. 404 si el curso no existe, 409 si el código está repetido o si los cupos quedan bajo los ocupados. 403 si no eres Admin.")
    @PutMapping("/{id}")
    public CursoResponse actualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest datos) {
        return servicio.actualizar(id, datos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Elimina un curso (solo Admin)",
            description = "204 si se elimina. 404 si no existe, 409 si el curso ya tiene órdenes. 403 si no eres Admin.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
