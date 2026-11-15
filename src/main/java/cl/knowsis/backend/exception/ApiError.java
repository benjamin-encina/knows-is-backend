package cl.knowsis.backend.exception;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.http.HttpStatus;

/**
 * Formato ÚNICO de error de toda la API. El frontend lee siempre "mensaje" (y "detalles" si existe).
 *
 * Ejemplo:
 * { "estado": 409, "error": "Conflict", "mensaje": "Ya existe un curso con el código 10001",
 *   "ruta": "/api/cursos", "fecha": "2026-11-20T15:30:00Z" }
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        int estado,
        String error,
        String mensaje,
        String ruta,
        Instant fecha,
        List<String> detalles) {

    public static ApiError de(HttpStatus status, String mensaje, String ruta, List<String> detalles) {
        return new ApiError(status.value(), status.getReasonPhrase(), mensaje, ruta, Instant.now(), detalles);
    }
}
