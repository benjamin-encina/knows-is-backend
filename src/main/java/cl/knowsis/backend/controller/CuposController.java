package cl.knowsis.backend.controller;

import cl.knowsis.backend.dto.ResumenCuposResponse;
import cl.knowsis.backend.service.CuposService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cupos", description = "Resumen de cupos ocupados y disponibles (solo Admin)")
public class CuposController {

    private final CuposService servicio;

    public CuposController(CuposService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/cupos")
    @Operation(summary = "Resumen de cupos totales, ocupados, disponibles y cursos con cupo crítico")
    public ResumenCuposResponse resumen() {
        return servicio.resumen();
    }
}
