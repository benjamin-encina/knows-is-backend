package cl.knowsis.backend.service;

import cl.knowsis.backend.dto.OrdenRequest;
import cl.knowsis.backend.dto.OrdenResponse;
import cl.knowsis.backend.exception.ConflictoException;
import cl.knowsis.backend.exception.DatosInvalidosException;
import cl.knowsis.backend.exception.RecursoNoEncontradoException;
import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.model.EstadoOrden;
import cl.knowsis.backend.model.Orden;
import cl.knowsis.backend.model.OrdenItem;
import cl.knowsis.backend.repository.CursoRepository;
import cl.knowsis.backend.repository.OrdenItemRepository;
import cl.knowsis.backend.repository.OrdenRepository;
import cl.knowsis.backend.util.Rut;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio de las compras (frente C).
 * Todo lo de crear() ocurre en UNA transacción: si algo falla (sin cupos, RUT, pago rechazado...)
 * no queda nada guardado y se liberan los bloqueos de los cursos.
 */
@Service
public class OrdenService {

    private final OrdenRepository ordenes;
    private final OrdenItemRepository items;
    private final CursoRepository cursos;
    private final PagoSimulado pagos;

    public OrdenService(OrdenRepository ordenes, OrdenItemRepository items,
                        CursoRepository cursos, PagoSimulado pagos) {
        this.ordenes = ordenes;
        this.items = items;
        this.cursos = cursos;
        this.pagos = pagos;
    }

    /**
     * Crea una orden pagada. Orden de las validaciones (contrato):
     * curso existe (404) → no inscrito (409) → con cupos (409) → RUT (400) → cobrar (422) → guardar.
     */
    @Transactional
    public OrdenResponse crear(Long usuarioId, OrdenRequest datos) {
        // Sin repetidos y en orden de id: todas las compras bloquean los cursos en el mismo orden (evita deadlocks)
        List<Long> ids = datos.cursoIds().stream().distinct().sorted().toList();

        List<Curso> comprados = new ArrayList<>();
        for (Long cursoId : ids) {
            // SELECT ... FOR UPDATE: otra compra del mismo curso espera hasta que esta termine
            Curso curso = ordenes.bloquearCurso(cursoId)
                    .orElseThrow(() -> RecursoNoEncontradoException.de("Curso", cursoId));
            if (items.existeCompra(usuarioId, cursoId, EstadoOrden.Pagada)) {
                throw new ConflictoException("Ya estás inscrito en el curso " + curso.getNombre());
            }
            if (cursos.contarCuposOcupados(cursoId) >= curso.getCupos()) {
                throw new ConflictoException("El curso " + curso.getNombre() + " no tiene cupos disponibles");
            }
            comprados.add(curso);
        }

        OrdenRequest.Facturacion f = datos.facturacion();
        if (!Rut.esValido(f.rut())) {
            throw new DatosInvalidosException("El RUT no es válido: revisa el dígito verificador.");
        }

        // El total lo calcula el servidor con los precios de la base de datos
        int subtotal = comprados.stream().mapToInt(Curso::getPrecio).sum();
        PlanPago plan = PlanPago.calcular(subtotal, datos.cuotas());

        // Si el pago se rechaza lanza ReglaNegocioException (422) y la orden no se guarda
        PagoSimulado.Pago pago = pagos.cobrar(datos.tarjeta().numero(), plan.total());

        Orden orden = new Orden();
        orden.setNumero("TMP-" + UUID.randomUUID().toString().substring(0, 8));   // provisional (único)
        orden.setUsuarioId(usuarioId);
        orden.setSubtotal(plan.subtotal());
        orden.setDescuento(plan.descuento());
        orden.setTotal(plan.total());
        orden.setCuotas(plan.cuotas());
        orden.setPrimeraCuota(plan.primeraCuota());
        orden.setValorCuota(plan.valorCuota());
        orden.setEstado(EstadoOrden.Pagada);
        orden.setFacturaNombre(f.nombre().trim());
        orden.setFacturaRut(Rut.formatear(f.rut()));
        orden.setFacturaCorreo(f.correo().trim());
        orden.setFacturaTelefono(f.telefono() == null || f.telefono().isBlank() ? null : f.telefono().trim());
        orden.setFacturaDireccion(f.direccion().trim());
        orden.setPagoMedio(pago.medio());
        orden.setPagoReferencia(pago.referencia());
        comprados.forEach(c -> orden.agregarItem(OrdenItem.de(c)));

        // El número correlativo sale del id autoincremental: KS-00001, KS-00002...
        // (dos compras simultáneas nunca obtienen el mismo id, así que el número no se repite)
        Orden guardada = ordenes.saveAndFlush(orden);
        guardada.setNumero(numeroDe(guardada.getId()));
        return OrdenResponse.de(guardada);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> misOrdenes(Long usuarioId) {
        return ordenes.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId).stream().map(OrdenResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listar() {
        return ordenes.findAllByOrderByFechaDescIdDesc().stream().map(OrdenResponse::de).toList();
    }

    /** Solo el dueño o un Admin pueden ver la orden; si no, 403. */
    @Transactional(readOnly = true)
    public OrdenResponse obtener(String numero, Long usuarioId, boolean esAdmin) {
        Orden orden = buscar(numero);
        if (!esAdmin && !orden.getUsuarioId().equals(usuarioId)) {
            throw new AccessDeniedException("La orden no te pertenece");
        }
        return OrdenResponse.de(orden);
    }

    /** Anula una orden pagada. Como los cupos se cuentan desde órdenes Pagadas, el cupo se libera solo. */
    @Transactional
    public OrdenResponse anular(String numero) {
        Orden orden = buscar(numero);
        if (orden.getEstado() == EstadoOrden.Anulada) {
            throw new ConflictoException("La orden " + numero + " ya está anulada");
        }
        orden.setEstado(EstadoOrden.Anulada);
        return OrdenResponse.de(ordenes.save(orden));
    }

    static String numeroDe(Long id) {
        return String.format("KS-%05d", id);
    }

    private Orden buscar(String numero) {
        return ordenes.findByNumero(numero)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Orden", numero));
    }
}
