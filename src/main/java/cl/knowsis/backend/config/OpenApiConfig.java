package cl.knowsis.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springframework.context.annotation.Configuration;

/**
 * Documentación Swagger: http://localhost:8080/swagger-ui.html
 *
 * Define el esquema "bearerAuth" para que aparezca el botón Authorize (JWT).
 * En los endpoints protegidos agrega:  @SecurityRequirement(name = "bearerAuth")
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Knows-IS API",
        version = "0.1.0",
        description = "API REST de la academia Knows-IS (DSY1104)"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega el accessToken que devuelve POST /api/auth/login")
public class OpenApiConfig {
}
