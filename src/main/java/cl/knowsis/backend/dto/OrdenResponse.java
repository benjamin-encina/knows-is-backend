package cl.knowsis.backend.dto;

import cl.knowsis.backend.model.EstadoOrden;
import cl.knowsis.backend.model.Orden;

import java.time.Instant;
import java.util.List;

/** Orden tal como la recibe el frontend (formato del contrato). "id" es el número, ej. KS-00001. */
public record OrdenResponse(
        String id,
        Long usuarioId,
        Instant fecha,
        List<Item> items,
        int subtotal,
        int descuento,
        int total,
        int cuotas,
        int primeraCuota,
        int valorCuota,
        Facturacion facturacion,
        Pago pago,
        EstadoOrden estado) {

    public record Item(Long cursoId, String codigo, String nombre, String instructor, String categoria, int precio) {}

    public record Facturacion(String nombre, String rut, String correo, String telefono, String direccion) {}

    public record Pago(String medio, String referencia) {}

    public static OrdenResponse de(Orden o) {
        List<Item> items = o.getItems().stream()
                .map(i -> new Item(i.getCursoId(), i.getCodigo(), i.getNombre(), i.getInstructor(),
                        i.getCategoria(), i.getPrecio()))
                .toList();
        return new OrdenResponse(o.getNumero(), o.getUsuarioId(), o.getFecha(), items,
                o.getSubtotal(), o.getDescuento(), o.getTotal(), o.getCuotas(), o.getPrimeraCuota(), o.getValorCuota(),
                new Facturacion(o.getFacturaNombre(), o.getFacturaRut(), o.getFacturaCorreo(),
                        o.getFacturaTelefono() == null ? "" : o.getFacturaTelefono(), o.getFacturaDireccion()),
                new Pago(o.getPagoMedio(), o.getPagoReferencia()),
                o.getEstado());
    }
}
