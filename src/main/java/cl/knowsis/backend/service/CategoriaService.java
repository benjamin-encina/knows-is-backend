package cl.knowsis.backend.service;

import cl.knowsis.backend.dto.CategoriaResponse;
import cl.knowsis.backend.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    public List<CategoriaResponse> listar() {
        return categorias.findAllByOrderByIdAsc().stream()
                .map(CategoriaResponse::de)
                .toList();
    }

}
