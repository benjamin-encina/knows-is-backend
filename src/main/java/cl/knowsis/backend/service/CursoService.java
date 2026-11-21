package cl.knowsis.backend.service;

import cl.knowsis.backend.dto.CursoRequest;
import cl.knowsis.backend.dto.CursoResponse;
import cl.knowsis.backend.exception.ConflictoException;
import cl.knowsis.backend.exception.RecursoNoEncontradoException;
import cl.knowsis.backend.exception.ReglaNegocioException;
import cl.knowsis.backend.model.Categoria;
import cl.knowsis.backend.model.Curso;
import cl.knowsis.backend.model.Nivel;
import cl.knowsis.backend.repository.CategoriaRepository;
import cl.knowsis.backend.repository.CursoRepository;
import cl.knowsis.backend.repository.CursoSpecs;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursos;
    private final CategoriaRepository categorias;

    public CursoService(CursoRepository cursos, CategoriaRepository categorias) {
        this.cursos = cursos;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listar(String categoria, Nivel nivel, String q) {
        Specification<Curso> filtro = (root, query, cb) -> cb.conjunction();
        if (categoria != null && !categoria.isBlank()) {
            filtro = filtro.and(CursoSpecs.categoria(categoria));
        }
        if (nivel != null) {
            filtro = filtro.and(CursoSpecs.nivel(nivel));
        }
        if (q != null && !q.isBlank()) {
            filtro = filtro.and(CursoSpecs.texto(q.trim()));
        }
        return cursos.findAll(filtro, Sort.by("id")).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public CursoResponse crear(CursoRequest datos) {
        String codigo = datos.codigo().trim();
        if (cursos.existsByCodigo(codigo)) {
            throw new ConflictoException("Ya existe un curso con el código " + codigo);
        }
        Curso curso = new Curso();
        aplicar(curso, datos);
        return aRespuesta(cursos.save(curso));
    }

    @Transactional
    public CursoResponse actualizar(Long id, CursoRequest datos) {
        Curso curso = buscar(id);
        String codigo = datos.codigo().trim();
        if (cursos.existsByCodigoAndIdNot(codigo, id)) {
            throw new ConflictoException("Ya existe otro curso con el código " + codigo);
        }
        long ocupados = cursos.contarCuposOcupados(id);
        if (datos.cupos() < ocupados) {
            throw new ConflictoException("No puedes dejar " + datos.cupos()
                    + " cupos: ya hay " + ocupados + " ocupados");
        }
        aplicar(curso, datos);
        return aRespuesta(cursos.save(curso));
    }

    @Transactional
    public void eliminar(Long id) {
        Curso curso = buscar(id);
        if (cursos.contarItemsDeOrdenes(id) > 0) {
            throw new ConflictoException("No se puede eliminar el curso: tiene órdenes asociadas");
        }
        cursos.delete(curso);
    }

    private Curso buscar(Long id) {
        return cursos.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Curso", id));
    }

    private void aplicar(Curso curso, CursoRequest datos) {
        int cupoCritico = datos.cupoCritico() == null ? 0 : datos.cupoCritico();
        if (cupoCritico > datos.cupos()) {
            throw new ReglaNegocioException("El cupo crítico no puede ser mayor que los cupos totales");
        }
        Categoria categoria = categorias.findBySlug(datos.categoria())
                .orElseThrow(() -> new ReglaNegocioException(
                        "La categoría '" + datos.categoria() + "' no existe"));

        curso.setCodigo(datos.codigo().trim());
        curso.setNombre(datos.nombre().trim());
        curso.setDescripcion(datos.descripcion() == null ? "" : datos.descripcion().trim());
        curso.setPrecio(datos.precio());
        curso.setCupos(datos.cupos());
        curso.setCupoCritico(cupoCritico);
        curso.setCategoria(categoria);
        curso.setNivel(datos.nivel());
        curso.setInstructor(datos.instructor().trim());
        curso.setImagen(datos.imagen());
        curso.setIcono(datos.icono());
        curso.setDestacado(datos.destacado());
        curso.setNuevo(datos.nuevo());
    }

    private CursoResponse aRespuesta(Curso curso) {
        return CursoResponse.de(curso, (int) cursos.contarCuposOcupados(curso.getId()));
    }
}
