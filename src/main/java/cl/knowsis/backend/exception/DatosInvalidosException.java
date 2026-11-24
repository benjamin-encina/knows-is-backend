package cl.knowsis.backend.exception;

/** Un dato de la petición no es válido y se detecta en el servicio (ej. RUT o cuotas) → HTTP 400. */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
