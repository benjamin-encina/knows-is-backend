package cl.knowsis.backend.service;

import cl.knowsis.backend.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Pasarela de pago SIMULADA (no se cobra nada de verdad):
 *   4242 4242 4242 4242 → aprobada
 *   4000 0000 0000 0002 → rechazada (422)
 *   cualquier otro número que pase Luhn → aprobada; si no pasa Luhn → rechazada (422)
 * Devuelve solo los últimos 4 dígitos y una referencia. El número completo y el CVV
 * no se guardan ni se escriben en logs.
 */
@Component
public class PagoSimulado {

    public static final String TARJETA_RECHAZADA = "4000000000000002";
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom azar = new SecureRandom();

    /** Resultado de un cobro aprobado. */
    public record Pago(String medio, String referencia) {}

    public Pago cobrar(String numeroTarjeta, int monto) {
        String digitos = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("\\D", "");
        if (digitos.length() < 13 || digitos.length() > 19 || !pasaLuhn(digitos)) {
            throw new ReglaNegocioException("El número de tarjeta no es válido.");
        }
        if (TARJETA_RECHAZADA.equals(digitos)) {
            throw new ReglaNegocioException("El banco rechazó el pago. Prueba con otra tarjeta.");
        }
        String ultimos4 = digitos.substring(digitos.length() - 4);
        return new Pago("Tarjeta •••• " + ultimos4, "PAY-" + codigo(8));
    }

    /** Algoritmo de Luhn (igual que pasaLuhn en src/utils/pagos.js). */
    public static boolean pasaLuhn(String digitos) {
        if (digitos == null || digitos.isEmpty() || !digitos.matches("\\d+")) return false;
        int suma = 0;
        boolean doble = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int n = digitos.charAt(i) - '0';
            if (doble) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            suma += n;
            doble = !doble;
        }
        return suma % 10 == 0;
    }

    private String codigo(int largo) {
        StringBuilder sb = new StringBuilder(largo);
        for (int i = 0; i < largo; i++) sb.append(LETRAS.charAt(azar.nextInt(LETRAS.length())));
        return sb.toString();
    }
}
