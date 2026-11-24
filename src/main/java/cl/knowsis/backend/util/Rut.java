package cl.knowsis.backend.util;

/**
 * RUT chileno con dígito verificador módulo 11. Mismas reglas que src/utils/rut.js del frontend:
 * cuerpo de 7 u 8 dígitos y DV 0-9 o K (acepta "12.345.678-5", "12345678-5" o "123456785").
 */
public final class Rut {

    private Rut() {
    }

    public static String limpiar(String valor) {
        return valor == null ? "" : valor.replaceAll("[^0-9kK]", "").toUpperCase();
    }

    public static String calcularDv(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return "0";
        if (resto == 10) return "K";
        return String.valueOf(resto);
    }

    public static boolean esValido(String valor) {
        String limpio = limpiar(valor);
        if (limpio.length() < 2) return false;
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        String dv = limpio.substring(limpio.length() - 1);
        if (cuerpo.length() < 7 || cuerpo.length() > 8 || !cuerpo.matches("\\d+")) return false;
        return calcularDv(cuerpo).equals(dv);
    }

    /** "123456785" → "12.345.678-5" (formato con que se guarda en la orden). */
    public static String formatear(String valor) {
        String limpio = limpiar(valor);
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        String dv = limpio.substring(limpio.length() - 1);
        return cuerpo.replaceAll("\\B(?=(\\d{3})+(?!\\d))", ".") + "-" + dv;
    }
}
