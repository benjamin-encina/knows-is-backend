package cl.knowsis.backend.exception;

/** La operación choca con el estado actual (dato repetido, ya inscrito, sin cupos...) → HTTP 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
