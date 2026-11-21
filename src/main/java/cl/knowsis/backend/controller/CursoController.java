package cl.knowsis.backend.controller;

import cl.knowsis.backend.dto.CursoRequest;
import cl.knowsis.backend.dto.CursoResponse;
import cl.knowsis.backend.model.Nivel;
import cl.knowsis.backend.service.CursoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos")

public class CursoController {

    private final CursoService servicio;

    public CursoController(CursoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<CursoResponse> listar(@RequestParam(required = false) String categoria,
                                      @RequestParam(required = false) Nivel nivel,
                                      @RequestParam(required = false) String q) {
        return servicio.listar(categoria, nivel, q);
    }

    @GetMapping("/{id}")
    public CursoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    // TODO: restringir a ADMIN cuando llegue la seguridad JWT de Kevin
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CursoResponse crear(@Valid @RequestBody CursoRequest datos) {
        return servicio.crear(datos);
    }

    // TODO: restringir a ADMIN
    @PutMapping("/{id}")
    public CursoResponse actualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest datos) {
        return servicio.actualizar(id, datos);
    }

    // TODO: restringir a ADMIN
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
