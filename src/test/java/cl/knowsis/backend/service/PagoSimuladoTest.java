package cl.knowsis.backend.service;

import cl.knowsis.backend.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagoSimuladoTest {

    private final PagoSimulado pagos = new PagoSimulado();

    @Test
    void tarjetaDemo4242_seApruebaYSoloGuardaLosUltimos4() {
        PagoSimulado.Pago pago = pagos.cobrar("4242 4242 4242 4242", 438_666);

        assertEquals("Tarjeta •••• 4242", pago.medio());
        assertTrue(pago.referencia().startsWith("PAY-"));
        assertFalse(pago.medio().contains("4242 4242"));
    }

    @Test
    void tarjetaDemoRechazada_lanzaReglaDeNegocio() {
        assertThrows(ReglaNegocioException.class, () -> pagos.cobrar("4000 0000 0000 0002", 1000));
    }

    @Test
    void otraTarjetaValidaPorLuhn_seAprueba() {
        PagoSimulado.Pago pago = pagos.cobrar("5555555555554444", 1000);
        assertEquals("Tarjeta •••• 4444", pago.medio());
    }

    @Test
    void numeroQueNoPasaLuhn_seRechaza() {
        assertThrows(ReglaNegocioException.class, () -> pagos.cobrar("4242 4242 4242 4241", 1000));
    }

    @Test
    void luhn() {
        assertTrue(PagoSimulado.pasaLuhn("4242424242424242"));
        assertFalse(PagoSimulado.pasaLuhn("1234567812345678"));
        assertFalse(PagoSimulado.pasaLuhn(""));
    }
}
