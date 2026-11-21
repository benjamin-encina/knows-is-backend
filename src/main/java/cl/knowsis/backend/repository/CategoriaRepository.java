package cl.knowsis.backend.repository;

import cl.knowsis.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository <Categoria, Long> {

    Optional<Categoria> findBySlug(String slug);

    List<Categoria> findAllByOrderByIdAsc();


}
