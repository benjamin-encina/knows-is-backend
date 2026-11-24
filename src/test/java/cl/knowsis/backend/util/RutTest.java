package cl.knowsis.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Mismas reglas que src/utils/rut.js del frontend. */
class RutTest {

    @Test
    void rutValidoConYSinFormato() {
        assertTrue(Rut.esValido("12.345.678-5"));
        assertTrue(Rut.esValido("123456785"));
        assertTrue(Rut.esValido("11.111.111-1"));
    }

    @Test
    void digitoVerificadorKyCero() {
        assertEquals("K", Rut.calcularDv("10000013"));
        assertTrue(Rut.esValido("10.000.013-K"));
        assertTrue(Rut.esValido("10000013k"));
        assertEquals("0", Rut.calcularDv("10000004"));
    }

    @Test
    void rutInvalido() {
        assertFalse(Rut.esValido("12.345.678-9"));
        assertFalse(Rut.esValido("123"));
        assertFalse(Rut.esValido(""));
        assertFalse(Rut.esValido(null));
    }

    @Test
    void formatear() {
        assertEquals("12.345.678-5", Rut.formatear("123456785"));
        assertEquals("1.234.567-4", Rut.formatear("1234567-4"));
    }
}
