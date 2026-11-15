package cl.knowsis.backend.exception;

/** El recurso pedido no existe → HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    /** Ejemplo: throw RecursoNoEncontradoException.de("Curso", 99);  → "Curso no encontrado: 99" */
    public static RecursoNoEncontradoException de(String recurso, Object id) {
        return new RecursoNoEncontradoException(recurso + " no encontrado: " + id);
    }
}
