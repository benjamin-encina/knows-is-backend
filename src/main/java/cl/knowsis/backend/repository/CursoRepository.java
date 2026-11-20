package cl.knowsis.backend.repository;

import cl.knowsis.backend.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CursoRepository extends JpaRepository<Curso, Long>, JpaSpecificationExecutor<Curso> {

    //para consultar si existe algun codigo con ese codigo
    boolean existsByCodigo(String codigo);

    //para revisar si existe ese codigo en otro curso distinto
    boolean existsByCodigoAndIdNot(String codigo, Long id);

}
