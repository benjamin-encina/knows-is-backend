package cl.knowsis.backend.dto;

import cl.knowsis.backend.model.Nivel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CursoRequest(

        @NotBlank(message = "El código es obligatorio")
        @Size(min = 3, max = 20, message = "El código debe tener entre 3 y 20 carácteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 carácteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 carácteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio no puede ser negativo")
        Integer precio,

        @NotNull(message = "Los cupos son obligatrorios")
        @Min(value = 0, message = "Los cupos no pueden ser negativos")
        Integer cupos,

        @Min(value = 0, message = "El cupo crítico no puede ser negativo")
        Integer cupoCritico,

        @NotNull(message = "La categoría es obligatoria")
        String categoria,

        @NotNull(message = "El nivel es obligatorio")
        Nivel nivel,

        @NotBlank(message = "El instructor es obligatorio")
        @Size(max = 100, message = "El instructor no puede superar los 100 caracteres")
        String instructor,

        @Size(max =  500, message = "La URL de la imagen no puede superar los 500 caracteres")
        String imagen,

        @Size(max = 60, message = "El ícono no puede superar los 60 caracteres")
        String icono,

        boolean destacado,

        boolean nuevo

) {
}
