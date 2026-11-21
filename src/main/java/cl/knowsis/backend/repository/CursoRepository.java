package cl.knowsis.backend.repository;

import cl.knowsis.backend.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CursoRepository extends JpaRepository<Curso, Long>, JpaSpecificationExecutor<Curso> {


    @Query(value = """
            SELECT COUNT(*) FROM orden_item oi
            JOIN orden o ON o.id = oi.orden_id
            WHERE oi.curso_id = :cursoId AND o.estado = 'Pagada'
            """, nativeQuery = true)
    long contarCuposOcupados(@Param("cursoId") Long cursoId);

    @Query(value = "SELECT COUNT(*) FROM orden_item WHERE curso_id = :cursoId", nativeQuery = true)
    long contarItemsDeOrdenes(@Param("cursoId") Long cursoId);

    //para consultar si existe algun codigo con ese codigo
    boolean existsByCodigo(String codigo);

    //para revisar si existe ese codigo en otro curso distinto
    boolean existsByCodigoAndIdNot(String codigo, Long id);

}
