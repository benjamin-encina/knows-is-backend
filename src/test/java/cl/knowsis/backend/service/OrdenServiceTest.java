package cl.knowsis.backend.service;

import cl.knowsis.backend.dto.OrdenRequest;
import cl.knowsis.backend.dto.OrdenResponse;
import cl.knowsis.backend.exception.ConflictoException;
import cl.knowsis.backend.exception.DatosInvalidosException;
import cl.knowsis.backend.exception.RecursoNoEncontradoException;
import cl.knowsis.backend.exception.ReglaNegocioException;
import cl.knowsis.backend.model.Categoria;
import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.model.EstadoOrden;
import cl.knowsis.backend.model.Orden;
import cl.knowsis.backend.repository.CursoRepository;
import cl.knowsis.backend.repository.OrdenItemRepository;
import cl.knowsis.backend.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    private static final Long ALUMNO = 4L;

    @Mock private OrdenRepository ordenes;
    @Mock private OrdenItemRepository items;
    @Mock private CursoRepository cursos;
    @Mock private PagoSimulado pagos;

    @InjectMocks
    private OrdenService servicio;

    private Curso curso(long id, String nombre, int precio, int cupos) {
        // Categoria tiene constructor protegido (es del frente A): se crea por reflexión, sin mocks
        Categoria categoria = BeanUtils.instantiateClass(Categoria.class);
        ReflectionTestUtils.setField(categoria, "slug", "programacion");
        Curso c = new Curso();
        ReflectionTestUtils.setField(c, "id", id);
        c.setCodigo("1000" + id);
        c.setNombre(nombre);
        c.setPrecio(precio);
        c.setCupos(cupos);
        c.setInstructor("Kevin Soto");
        c.setCategoria(categoria);
        return c;
    }

    private OrdenRequest pedido(List<Long> cursoIds, int cuotas, String rut, String tarjeta) {
        return new OrdenRequest(cursoIds, cuotas,
                new OrdenRequest.Facturacion("Camila Rojas", rut, "camila@knowsis.cl", "", "Av. Siempre Viva 742"),
                new OrdenRequest.Tarjeta(tarjeta, "CAMILA ROJAS", "12/28", "123"));
    }

    private OrdenRequest pedidoValido(List<Long> cursoIds, int cuotas) {
        return pedido(cursoIds, cuotas, "12.345.678-5", "4242 4242 4242 4242");
    }

    @Test
    void crear_dosCursosEnTresCuotas_calculaElTotalEnElServidorYNumeraKS00001() {
        when(ordenes.bloquearCurso(1L)).thenReturn(Optional.of(curso(1, "JavaScript desde cero", 210_000, 30)));
        when(ordenes.bloquearCurso(3L)).thenReturn(Optional.of(curso(3, "Redes", 260_000, 30)));
        when(pagos.cobrar(anyString(), anyInt())).thenReturn(new PagoSimulado.Pago("Tarjeta •••• 4242", "PAY-TEST"));
        when(ordenes.saveAndFlush(any(Orden.class))).thenAnswer(inv -> {
            Orden o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrdenResponse r = servicio.crear(ALUMNO, pedidoValido(List.of(3L, 1L, 1L), 3));

        assertEquals("KS-00001", r.id());
        assertEquals(ALUMNO, r.usuarioId());
        assertEquals(2, r.items().size());                // el curso repetido se cuenta una vez
        assertEquals(470_000, r.subtotal());
        assertEquals(31_334, r.descuento());
        assertEquals(438_666, r.total());
        assertEquals(125_334, r.primeraCuota());
        assertEquals(156_666, r.valorCuota());
        assertEquals(EstadoOrden.Pagada, r.estado());
        assertEquals("Tarjeta •••• 4242", r.pago().medio());
        verify(pagos).cobrar("4242 4242 4242 4242", 438_666);
    }

    @Test
    void crear_cursoInexistente_lanza404SinCobrar() {
        when(ordenes.bloquearCurso(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.crear(ALUMNO, pedidoValido(List.of(99L), 1)));
        verify(pagos, never()).cobrar(anyString(), anyInt());
    }

    @Test
    void crear_cursoYaComprado_noSePuedeComprarDosVeces() {
        when(ordenes.bloquearCurso(1L)).thenReturn(Optional.of(curso(1, "JavaScript desde cero", 210_000, 30)));
        when(items.existeCompra(ALUMNO, 1L, EstadoOrden.Pagada)).thenReturn(true);

        ConflictoException e = assertThrows(ConflictoException.class,
                () -> servicio.crear(ALUMNO, pedidoValido(List.of(1L), 1)));

        assertEquals("Ya estás inscrito en el curso JavaScript desde cero", e.getMessage());
        verify(pagos, never()).cobrar(anyString(), anyInt());
        verify(ordenes, never()).saveAndFlush(any());
    }

    @Test
    void crear_sinCuposDisponibles_lanza409() {
        when(ordenes.bloquearCurso(5L)).thenReturn(Optional.of(curso(5, "Ciberseguridad", 300_000, 8)));
        when(cursos.contarCuposOcupados(5L)).thenReturn(8L);

        assertThrows(ConflictoException.class, () -> servicio.crear(ALUMNO, pedidoValido(List.of(5L), 1)));
        verify(pagos, never()).cobrar(anyString(), anyInt());
    }

    @Test
    void crear_conElUltimoCupo_sePermite() {
        when(ordenes.bloquearCurso(5L)).thenReturn(Optional.of(curso(5, "Ciberseguridad", 300_000, 8)));
        when(cursos.contarCuposOcupados(5L)).thenReturn(7L);
        when(pagos.cobrar(anyString(), anyInt())).thenReturn(new PagoSimulado.Pago("Tarjeta •••• 4242", "PAY-TEST"));
        when(ordenes.saveAndFlush(any(Orden.class))).thenAnswer(inv -> {
            Orden o = inv.getArgument(0);
            o.setId(12L);
            return o;
        });

        assertEquals("KS-00012", servicio.crear(ALUMNO, pedidoValido(List.of(5L), 1)).id());
    }

    @Test
    void crear_rutInvalido_lanza400SinCobrar() {
        when(ordenes.bloquearCurso(1L)).thenReturn(Optional.of(curso(1, "JavaScript desde cero", 210_000, 30)));

        assertThrows(DatosInvalidosException.class,
                () -> servicio.crear(ALUMNO, pedido(List.of(1L), 1, "12.345.678-9", "4242 4242 4242 4242")));
        verify(pagos, never()).cobrar(anyString(), anyInt());
    }

    @Test
    void crear_pagoRechazado_noGuardaLaOrden() {
        when(ordenes.bloquearCurso(1L)).thenReturn(Optional.of(curso(1, "JavaScript desde cero", 210_000, 30)));
        when(pagos.cobrar(anyString(), anyInt())).thenThrow(new ReglaNegocioException("El banco rechazó el pago."));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(ALUMNO, pedido(List.of(1L), 1, "12.345.678-5", "4000 0000 0000 0002")));
        verify(ordenes, never()).saveAndFlush(any());
    }

    @Test
    void obtener_ordenAjenaSinSerAdmin_lanza403() {
        Orden orden = new Orden();
        orden.setNumero("KS-00001");
        orden.setUsuarioId(7L);
        when(ordenes.findByNumero("KS-00001")).thenReturn(Optional.of(orden));

        assertThrows(AccessDeniedException.class, () -> servicio.obtener("KS-00001", ALUMNO, false));
        assertEquals("KS-00001", servicio.obtener("KS-00001", 99L, true).id());   // el admin sí puede
    }

    @Test
    void anular_cambiaElEstadoYNoSePuedeAnularDosVeces() {
        Orden orden = new Orden();
        orden.setNumero("KS-00001");
        orden.setUsuarioId(ALUMNO);
        when(ordenes.findByNumero("KS-00001")).thenReturn(Optional.of(orden));
        when(ordenes.save(orden)).thenReturn(orden);

        assertEquals(EstadoOrden.Anulada, servicio.anular("KS-00001").estado());
        assertThrows(ConflictoException.class, () -> servicio.anular("KS-00001"));
    }

    @Test
    void numeroCorrelativo() {
        assertEquals("KS-00001", OrdenService.numeroDe(1L));
        assertEquals("KS-12345", OrdenService.numeroDe(12345L));
    }
}
