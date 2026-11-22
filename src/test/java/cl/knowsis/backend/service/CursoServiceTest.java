package cl.knowsis.backend.service;


import cl.knowsis.backend.dto.CursoRequest;
import cl.knowsis.backend.dto.CursoResponse;
import cl.knowsis.backend.exception.ConflictoException;
import cl.knowsis.backend.exception.RecursoNoEncontradoException;
import cl.knowsis.backend.exception.ReglaNegocioException;
import cl.knowsis.backend.model.Categoria;
import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.model.Nivel;
import cl.knowsis.backend.repository.CategoriaRepository;
import cl.knowsis.backend.repository.CursoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository cursos;

    @Mock
    private CategoriaRepository categorias;

    @InjectMocks
    private CursoService servicio;

    // Arma un CursoRequest de prueba; así, si cambian los campos, se ajusta en un solo lugar
    private CursoRequest datos(String codigo, int cupos, int cupoCritico, String categoria) {
        return new CursoRequest(codigo, "Curso de prueba", "Descripción", 15000, cupos, cupoCritico,
                categoria, Nivel.Inicial, "Ghislaine Carrasco", null, null, false, true);
    }

    @Test
    void crear_guardaElCursoYDevuelveLaRespuesta() {
        when(categorias.findBySlug("programacion")).thenReturn(Optional.of(mock(Categoria.class)));
        when(cursos.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

        CursoResponse respuesta = servicio.crear(datos("TEST-01", 20, 3, "programacion"));

        assertEquals("TEST-01", respuesta.codigo());
        verify(cursos).save(any(Curso.class));
    }

    @Test
    void crear_conCodigoRepetido_lanzaConflicto() {
        when(cursos.existsByCodigo("TEST-01")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> servicio.crear(datos("TEST-01", 20, 3, "programacion")));
        verify(cursos, never()).save(any());
    }

    @Test
    void crear_conCupoCriticoMayorQueLosCupos_lanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(datos("TEST-02", 2, 5, "programacion")));
        verify(cursos, never()).save(any());
    }

    @Test
    void crear_conCategoriaInexistente_lanzaReglaDeNegocio() {
        when(categorias.findBySlug("inexistente")).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(datos("TEST-03", 20, 3, "inexistente")));
    }

    @Test
    void obtener_conIdInexistente_lanzaNoEncontrado() {
        when(cursos.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(99L));
    }

    @Test
    void actualizar_bajandoLosCuposPorDebajoDeLosOcupados_lanzaConflicto() {
        when(cursos.findById(1L)).thenReturn(Optional.of(new Curso()));
        when(cursos.contarCuposOcupados(1L)).thenReturn(8L);

        assertThrows(ConflictoException.class,
                () -> servicio.actualizar(1L, datos("10001", 5, 3, "programacion")));
        verify(cursos, never()).save(any());
    }

    @Test
    void eliminar_cursoConCompras_lanzaConflicto() {
        when(cursos.findById(1L)).thenReturn(Optional.of(new Curso()));
        when(cursos.contarItemsDeOrdenes(1L)).thenReturn(2L);

        assertThrows(ConflictoException.class, () -> servicio.eliminar(1L));
        verify(cursos, never()).delete(any(Curso.class));
    }
}