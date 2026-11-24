package cl.knowsis.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Cuerpo de POST /api/ordenes. No trae precios ni totales: el servidor los calcula
 * con los precios de la base de datos (si el cliente los manda, se ignoran).
 */
public record OrdenRequest(
        @NotEmpty(message = "debe incluir al menos un curso") List<@NotNull Long> cursoIds,
        @NotNull Integer cuotas,
        @NotNull @Valid Facturacion facturacion,
        @NotNull @Valid Tarjeta tarjeta) {

    public record Facturacion(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank String rut,
            @NotBlank @Email @Size(max = 100) String correo,
            @Size(max = 20) String telefono,
            @NotBlank @Size(max = 300) String direccion) {}

    /** Datos de la tarjeta: se usan solo para el cobro simulado y NO se guardan. */
    public record Tarjeta(
            @NotBlank String numero,
            @NotBlank @Size(max = 60) String titular,
            @NotBlank @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "usa el formato MM/AA") String vencimiento,
            @NotBlank @Pattern(regexp = "^\\d{3,4}$", message = "debe tener 3 o 4 dígitos") String cvv) {

        /** Evita que el número o el CVV aparezcan en logs si alguien imprime el objeto. */
        @Override
        public String toString() {
            String d = numero == null ? "" : numero.replaceAll("\\D", "");
            return "Tarjeta[•••• " + (d.length() >= 4 ? d.substring(d.length() - 4) : "") + "]";
        }
    }
}
