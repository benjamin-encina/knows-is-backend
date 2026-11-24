package cl.knowsis.backend.dto;

import java.util.List;

/** Respuesta de GET /api/admin/cupos (formato del contrato). */
public record ResumenCuposResponse(
        int cuposTotales,
        int cuposOcupados,
        int cuposDisponibles,
        int cursosConCupoCritico,
        List<PorCurso> porCurso) {

    public record PorCurso(Long cursoId, String nombre, int cupos, int ocupados, int disponibles, boolean critico) {}
}
