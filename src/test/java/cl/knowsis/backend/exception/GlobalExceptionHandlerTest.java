package cl.knowsis.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

/** Comprueba que cada excepción se convierte en el código HTTP y el formato de error acordados. */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/api/cursos/99");

    @Test
    void recursoNoEncontradoDevuelve404ConElMensaje() {
        ResponseEntity<ApiError> respuesta = handler.noEncontrado(RecursoNoEncontradoException.de("Curso", 99), peticion);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Curso no encontrado: 99", respuesta.getBody().mensaje());
        assertEquals(404, respuesta.getBody().estado());
        assertEquals("/api/cursos/99", respuesta.getBody().ruta());
        assertNotNull(respuesta.getBody().fecha());
    }

    @Test
    void conflictoDevuelve409() {
        ResponseEntity<ApiError> respuesta = handler.conflicto(new ConflictoException("Código repetido"), peticion);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("Código repetido", respuesta.getBody().mensaje());
    }

    @Test
    void reglaDeNegocioDevuelve422() {
        ResponseEntity<ApiError> respuesta = handler.reglaNegocio(new ReglaNegocioException("Pago rechazado"), peticion);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, respuesta.getStatusCode());
        assertEquals("Pago rechazado", respuesta.getBody().mensaje());
    }

    @Test
    void accesoDenegadoDevuelve403() {
        ResponseEntity<ApiError> respuesta = handler.accesoDenegado(new AccessDeniedException("x"), peticion);

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void errorInesperadoDevuelve500SinFiltrarDetallesInternos() {
        ResponseEntity<ApiError> respuesta = handler.inesperado(new IllegalStateException("clave secreta de la BD"), peticion);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertFalse(respuesta.getBody().mensaje().contains("clave secreta"));
    }
}
