package cl.knowsis.backend.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de compra (frente C). Tabla "orden" creada en V1.
 * Del pago solo se guarda el medio ("Tarjeta •••• 4242") y la referencia: nunca el número completo ni el CVV.
 */
@Entity
@Table(name = "orden")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número visible para el frontend, por ejemplo KS-00001. */
    @Column(nullable = false, unique = true, length = 12)
    private String numero;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false) private int subtotal;
    @Column(nullable = false) private int descuento;
    @Column(nullable = false) private int total;
    @Column(nullable = false) private int cuotas;
    @Column(name = "primera_cuota", nullable = false) private int primeraCuota;
    @Column(name = "valor_cuota", nullable = false) private int valorCuota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoOrden estado = EstadoOrden.Pagada;

    @Column(name = "factura_nombre", nullable = false, length = 100) private String facturaNombre;
    @Column(name = "factura_rut", nullable = false, length = 12) private String facturaRut;
    @Column(name = "factura_correo", nullable = false, length = 100) private String facturaCorreo;
    @Column(name = "factura_telefono", length = 20) private String facturaTelefono;
    @Column(name = "factura_direccion", nullable = false, length = 300) private String facturaDireccion;

    @Column(name = "pago_medio", nullable = false, length = 40) private String pagoMedio;
    @Column(name = "pago_referencia", nullable = false, length = 40) private String pagoReferencia;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<OrdenItem> items = new ArrayList<>();

    @PrePersist
    void alCrear() { if (fecha == null) fecha = Instant.now(); }

    public void agregarItem(OrdenItem item) {
        item.setOrden(this);
        items.add(item);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }

    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

    public int getDescuento() { return descuento; }
    public void setDescuento(int descuento) { this.descuento = descuento; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getCuotas() { return cuotas; }
    public void setCuotas(int cuotas) { this.cuotas = cuotas; }

    public int getPrimeraCuota() { return primeraCuota; }
    public void setPrimeraCuota(int primeraCuota) { this.primeraCuota = primeraCuota; }

    public int getValorCuota() { return valorCuota; }
    public void setValorCuota(int valorCuota) { this.valorCuota = valorCuota; }

    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }

    public String getFacturaNombre() { return facturaNombre; }
    public void setFacturaNombre(String facturaNombre) { this.facturaNombre = facturaNombre; }

    public String getFacturaRut() { return facturaRut; }
    public void setFacturaRut(String facturaRut) { this.facturaRut = facturaRut; }

    public String getFacturaCorreo() { return facturaCorreo; }
    public void setFacturaCorreo(String facturaCorreo) { this.facturaCorreo = facturaCorreo; }

    public String getFacturaTelefono() { return facturaTelefono; }
    public void setFacturaTelefono(String facturaTelefono) { this.facturaTelefono = facturaTelefono; }

    public String getFacturaDireccion() { return facturaDireccion; }
    public void setFacturaDireccion(String facturaDireccion) { this.facturaDireccion = facturaDireccion; }

    public String getPagoMedio() { return pagoMedio; }
    public void setPagoMedio(String pagoMedio) { this.pagoMedio = pagoMedio; }

    public String getPagoReferencia() { return pagoReferencia; }
    public void setPagoReferencia(String pagoReferencia) { this.pagoReferencia = pagoReferencia; }

    public List<OrdenItem> getItems() { return items; }
}
