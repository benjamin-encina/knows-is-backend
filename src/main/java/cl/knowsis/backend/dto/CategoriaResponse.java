package cl.knowsis.backend.dto;

import ch.qos.logback.core.util.DefaultInvocationGate;
import cl.knowsis.backend.model.Categoria;

public record CategoriaResponse(Long id, String slug, String nombre, boolean enMenu) {

    public static CategoriaResponse de(Categoria categoria){

        return new CategoriaResponse(categoria.getId(), categoria.getSlug(),
                categoria.getNombre(), categoria.isEnMenu());
    }

}
