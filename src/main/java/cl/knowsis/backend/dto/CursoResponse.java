package cl.knowsis.backend.dto;

import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.model.Nivel;

public record CursoResponse(

        Long id,
        String codigo,
        String nombre,
        String descripcion,
        int precio,
        int cupos,
        int cupoCritico,
        String categoria,
        Nivel nivel,
        String instructor,
        String imagen,
        String icono,
        boolean destacado,
        boolean nuevo,
        int cuposOcupados,
        int cuposDisponibles
) {

    public static CursoResponse de(Curso curso, int cuposOcupados){

        return new CursoResponse(
                curso.getId(), curso.getCodigo(), curso.getNombre(), curso.getDescripcion(),
                curso.getPrecio(), curso.getCupos(), curso.getCupoCritico(),
                curso.getCategoria().getSlug(), curso.getNivel(), curso.getInstructor(),
                curso.getImagen(), curso.getIcono(), curso.isDestacado(), curso.isNuevo(),
                cuposOcupados, curso.getCupos() - cuposOcupados);
    }

}
