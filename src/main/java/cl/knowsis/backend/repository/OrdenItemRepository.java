package cl.knowsis.backend.repository;

import cl.knowsis.backend.model.EstadoOrden;
import cl.knowsis.backend.model.OrdenItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdenItemRepository extends JpaRepository<OrdenItem, Long> {

    /** true si el usuario ya tiene el curso en una orden con ese estado (Pagada = inscrito). */
    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM OrdenItem i
            WHERE i.orden.usuarioId = :usuarioId AND i.cursoId = :cursoId AND i.orden.estado = :estado
            """)
    boolean existeCompra(@Param("usuarioId") Long usuarioId,
                         @Param("cursoId") Long cursoId,
                         @Param("estado") EstadoOrden estado);
}
