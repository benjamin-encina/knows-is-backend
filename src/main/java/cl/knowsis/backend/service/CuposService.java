package cl.knowsis.backend.service;

import cl.knowsis.backend.dto.ResumenCuposResponse;
import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.repository.CursoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Resumen de cupos para el panel admin.
 * ocupados = ítems de órdenes Pagada del curso (CursoRepository.contarCuposOcupados, la misma consulta
 * que usa CursoResponse) · disponibles = cupos − ocupados · crítico = disponibles ≤ cupoCritico.
 */
@Service
public class CuposService {

    private final CursoRepository cursos;

    public CuposService(CursoRepository cursos) {
        this.cursos = cursos;
    }

    @Transactional(readOnly = true)
    public ResumenCuposResponse resumen() {
        List<ResumenCuposResponse.PorCurso> porCurso = cursos.findAll(Sort.by("id")).stream()
                .map(this::deCurso)
                .toList();
        int totales = porCurso.stream().mapToInt(ResumenCuposResponse.PorCurso::cupos).sum();
        int ocupados = porCurso.stream().mapToInt(ResumenCuposResponse.PorCurso::ocupados).sum();
        int criticos = (int) porCurso.stream().filter(ResumenCuposResponse.PorCurso::critico).count();
        return new ResumenCuposResponse(totales, ocupados, totales - ocupados, criticos, porCurso);
    }

    private ResumenCuposResponse.PorCurso deCurso(Curso c) {
        int ocupados = (int) Math.min(cursos.contarCuposOcupados(c.getId()), c.getCupos());
        int disponibles = c.getCupos() - ocupados;
        return new ResumenCuposResponse.PorCurso(c.getId(), c.getNombre(), c.getCupos(), ocupados, disponibles,
                disponibles <= c.getCupoCritico());
    }
}
