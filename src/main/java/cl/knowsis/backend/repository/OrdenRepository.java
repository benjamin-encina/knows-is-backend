package cl.knowsis.backend.repository;

import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.model.Orden;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Optional<Orden> findByNumero(String numero);

    List<Orden> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);

    List<Orden> findAllByOrderByFechaDescIdDesc();

    /**
     * Lee el curso con SELECT ... FOR UPDATE: mientras dure la transacción de la compra,
     * otra compra del mismo curso espera. Así dos alumnos no se llevan el último cupo a la vez.
     * (Está aquí y no en CursoRepository para no tocar el código del frente A.)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Curso c WHERE c.id = :id")
    Optional<Curso> bloquearCurso(@Param("id") Long id);
}
