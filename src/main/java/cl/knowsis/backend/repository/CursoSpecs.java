package cl.knowsis.backend.repository;

import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.model.Nivel;
import org.springframework.data.jpa.domain.Specification;

public class CursoSpecs {

    private CursoSpecs(){
    }

    public static Specification<Curso> categoria(String slug) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("slug"), slug);
    }

    public static Specification<Curso> nivel(Nivel nivel){
        return (root, query, cb) -> cb.equal(root.get("nivel"), nivel);
    }

    public static Specification<Curso> texto(String q) {
        String patron = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron),
                cb.like(cb.lower(root.get("descripcion")), patron),
                cb.like(cb.lower(root.get("codigo")), patron),
                cb.like(cb.lower(root.get("instructor")), patron)
        );

    }


}
