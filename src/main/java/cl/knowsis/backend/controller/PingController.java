package cl.knowsis.backend.controller;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prueba rápida: si GET /api/ping responde, la API y Swagger están funcionando.
 * Sirve además como ejemplo de cómo documentar un endpoint con @Tag y @Operation.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Sistema", description = "Estado de la API")
public class PingController {

    @GetMapping("/ping")
    @Operation(summary = "Comprueba que la API está funcionando")
    public Map<String, Object> ping() {
        return Map.of(
                "estado", "ok",
                "aplicacion", "knows-is-backend",
                "fecha", Instant.now().toString());
    }
}
