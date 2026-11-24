package cl.knowsis.backend.model;

import jakarta.persistence.*;

/**
 * Curso comprado dentro de una orden. Guarda una COPIA de los datos del curso al momento de pagar,
 * así el historial no cambia si después el admin edita el curso.
 */
@Entity
@Table(name = "orden_item")
public class OrdenItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Column(nullable = false, length = 20)  private String codigo;
    @Column(nullable = false, length = 150) private String nombre;
    @Column(nullable = false, length = 100) private String instructor;
    @Column(nullable = false, length = 40)  private String categoria;
    @Column(nullable = false)               private int precio;

    public OrdenItem() {
    }

    /** Copia los datos actuales del curso. */
    public static OrdenItem de(Curso curso) {
        OrdenItem item = new OrdenItem();
        item.cursoId = curso.getId();
        item.codigo = curso.getCodigo();
        item.nombre = curso.getNombre();
        item.instructor = curso.getInstructor();
        item.categoria = curso.getCategoria().getSlug();
        item.precio = curso.getPrecio();
        return item;
    }

    public Long getId() { return id; }

    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }

    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }
}
