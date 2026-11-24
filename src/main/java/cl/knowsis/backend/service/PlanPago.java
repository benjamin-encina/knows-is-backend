package cl.knowsis.backend.service;

import cl.knowsis.backend.exception.DatosInvalidosException;

import java.util.Set;

/**
 * Plan de pago sin interés. Mismas fórmulas que planPago() en src/utils/pagos.js y que el contrato:
 *   valorCuota   = floor(subtotal / cuotas)
 *   primeraBruta = subtotal − valorCuota × (cuotas − 1)
 *   descuento    = round(primeraBruta × 0.20)     (solo con 3 o 6 cuotas; el pago único no lleva)
 *   total        = subtotal − descuento
 *   primeraCuota = primeraBruta − descuento
 * Ejemplo: 470.000 en 3 cuotas → descuento 31.334, total 438.666, 1ª 125.334, resto 156.666.
 */
public record PlanPago(int subtotal, int cuotas, int descuento, int total, int primeraCuota, int valorCuota) {

    public static final Set<Integer> CUOTAS_PERMITIDAS = Set.of(1, 3, 6);
    public static final double DESCUENTO_PRIMERA_CUOTA = 0.20;

    public static PlanPago calcular(int subtotal, int cuotas) {
        if (!CUOTAS_PERMITIDAS.contains(cuotas)) {
            throw new DatosInvalidosException("Cuotas permitidas: 1, 3 o 6");
        }
        if (subtotal < 0) {
            throw new DatosInvalidosException("El subtotal no puede ser negativo");
        }
        if (cuotas == 1) {
            return new PlanPago(subtotal, 1, 0, subtotal, subtotal, subtotal);
        }
        int valorCuota = subtotal / cuotas;                      // división entera = floor (subtotal ≥ 0)
        int primeraBruta = subtotal - valorCuota * (cuotas - 1);
        int descuento = (int) Math.round(primeraBruta * DESCUENTO_PRIMERA_CUOTA);
        return new PlanPago(subtotal, cuotas, descuento, subtotal - descuento, primeraBruta - descuento, valorCuota);
    }
}
