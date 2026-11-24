package cl.knowsis.backend.service;

import cl.knowsis.backend.dto.ResumenCuposResponse;
import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.repository.CursoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuposServiceTest {

    @Mock private CursoRepository cursos;
    @InjectMocks private CuposService servicio;

    private Curso curso(long id, int cupos, int critico) {
        Curso c = new Curso();
        ReflectionTestUtils.setField(c, "id", id);
        c.setNombre("Curso " + id);
        c.setCupos(cupos);
        c.setCupoCritico(critico);
        return c;
    }

    @Test
    void resumen_cuentaOcupadosDisponiblesYCriticos() {
        when(cursos.findAll(any(Sort.class))).thenReturn(List.of(curso(1, 30, 5), curso(2, 8, 3)));
        when(cursos.contarCuposOcupados(1L)).thenReturn(3L);
        when(cursos.contarCuposOcupados(2L)).thenReturn(6L);

        ResumenCuposResponse r = servicio.resumen();

        assertEquals(38, r.cuposTotales());
        assertEquals(9, r.cuposOcupados());
        assertEquals(29, r.cuposDisponibles());
        assertEquals(1, r.cursosConCupoCritico());
        assertEquals(27, r.porCurso().get(0).disponibles());
        assertFalse(r.porCurso().get(0).critico());
        assertTrue(r.porCurso().get(1).critico());   // quedan 2 y el crítico es 3
    }
}
