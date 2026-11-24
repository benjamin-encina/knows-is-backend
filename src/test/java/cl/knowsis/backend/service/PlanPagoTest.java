package cl.knowsis.backend.service;

import cl.knowsis.backend.exception.DatosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Mismos valores que planPago() del frontend (src/utils/pagos.js). */
class PlanPagoTest {

    @Test
    void tresCuotas_ejemploDelContrato() {
        PlanPago plan = PlanPago.calcular(470_000, 3);

        assertEquals(470_000, plan.subtotal());
        assertEquals(156_666, plan.valorCuota());
        assertEquals(31_334, plan.descuento());
        assertEquals(438_666, plan.total());
        assertEquals(125_334, plan.primeraCuota());
    }

    @Test
    void pagoUnico_noLlevaDescuento() {
        PlanPago plan = PlanPago.calcular(470_000, 1);

        assertEquals(0, plan.descuento());
        assertEquals(470_000, plan.total());
        assertEquals(470_000, plan.primeraCuota());
    }

    @Test
    void seisCuotas_descuentoSoloEnLaPrimera() {
        PlanPago plan = PlanPago.calcular(210_000, 6);

        assertEquals(35_000, plan.valorCuota());
        assertEquals(7_000, plan.descuento());
        assertEquals(203_000, plan.total());
        assertEquals(28_000, plan.primeraCuota());
        // la suma de las cuotas es el total
        assertEquals(plan.total(), plan.primeraCuota() + plan.valorCuota() * 5);
    }

    @Test
    void cuotasNoPermitidas_lanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> PlanPago.calcular(100_000, 2));
        assertThrows(DatosInvalidosException.class, () -> PlanPago.calcular(100_000, 12));
    }
}
