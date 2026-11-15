package cl.knowsis.backend.exception;

/** Los datos son válidos pero no cumplen una regla de negocio (ej. pago rechazado) → HTTP 422. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
