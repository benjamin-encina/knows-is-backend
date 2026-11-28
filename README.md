# Knows-IS · Backend (API REST)

API REST de la academia Knows-IS (DSY1104, Evaluación Parcial 3). Spring Boot 3 · Java 21 · MySQL 8 · Flyway · JWT · Swagger.
El frontend está en el repositorio `knows-is` (React + Vite).

## Requisitos

- Java 21
- Maven 3.9 o superior (o IntelliJ IDEA, que ya lo trae)
- Docker (para la base de datos) **o** MySQL 8 instalado (ver más abajo)

## Puesta en marcha (5 pasos)

1. **Base de datos.** En la carpeta del proyecto: `docker compose up -d`
   (crea MySQL 8.4 con la base `knowsis`, usuario `knowsis` y clave `knowsis`).
2. **Variables de entorno.** Copia `.env.example` como `.env` (en PowerShell: `Copy-Item .env.example .env`).
   Cambia `JWT_SECRET` por una clave propia de al menos 32 caracteres. El archivo `.env` no se sube a GitHub.
3. **Ejecutar.** `mvn spring-boot:run` (o el botón Run de IntelliJ sobre `KnowsIsApplication`).
   Flyway crea las tablas solo, la primera vez.
4. **Comprobar.** Abre `http://localhost:8080/api/ping` (debe responder `{"estado":"ok",...}`)
   y `http://localhost:8080/swagger-ui.html` (documentación de la API).
5. **Pruebas.** `mvn test`

> El proyecto no incluye `mvnw`. Si prefieres usarlo, genera el wrapper una vez con `mvn wrapper:wrapper`
> y súbelo al repositorio.

### Si usas un MySQL instalado en tu computador (sin Docker)

Crea la base y el usuario una vez, desde MySQL Workbench o la consola (`mysql -u root -p`):

```sql
CREATE DATABASE knowsis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'knowsis'@'%' IDENTIFIED BY 'knowsis';
GRANT ALL PRIVILEGES ON knowsis.* TO 'knowsis'@'%';
```

Si tu usuario o clave son otros, cámbialos en `.env` (`DB_USER` y `DB_PASSWORD`).

## Estructura

```
src/main/java/cl/knowsis/backend
├── KnowsIsApplication.java   arranque
├── config/                   CORS y Swagger (ya listos)
├── security/                 SecurityConfig (TEMPORAL: permite todo) → JWT lo agrega el frente B
├── exception/                ApiError, excepciones propias y GlobalExceptionHandler (ya listos)
├── controller/               endpoints REST (PingController de ejemplo)
├── service/                  lógica de negocio
├── repository/               acceso a la base de datos (JpaRepository)
├── model/                    entidades JPA
└── dto/                      objetos de entrada y salida de la API
src/main/resources
├── application.yml           configuración (lee las variables del archivo .env)
└── db/migration/             migraciones de Flyway (V1 = esquema base)
docs/contrato-api.md          acuerdo con el frontend: endpoints, JSON, permisos y reglas
```

## Cómo agregar una funcionalidad

1. Revisa en `docs/contrato-api.md` cómo debe ser el endpoint (si hay que cambiarlo, avisa al equipo antes).
2. `model/`: la entidad JPA con los mismos nombres de columna de la migración.
3. `repository/`: una interfaz que extienda `JpaRepository`.
4. `dto/`: records de entrada (con `@NotBlank`, `@Min`...) y de salida.
5. `service/`: las reglas de negocio. Para fallar, lanza `RecursoNoEncontradoException` (404),
   `ConflictoException` (409) o `ReglaNegocioException` (422): el `GlobalExceptionHandler` arma la respuesta.
6. `controller/`: recibe, llama al servicio y responde. Documenta con `@Tag` y `@Operation`; en los endpoints que
   exigen sesión agrega `@SecurityRequirement(name = "bearerAuth")`.
7. Pruébalo en Swagger y escribe al menos una prueba.

## Migraciones (Flyway)

- Cada frente usa su rango.
- Nombre: `V10__descripcion_corta.sql` (dos guiones bajos) dentro de `src/main/resources/db/migration`.
- Si Flyway se queja en tu computador, borra la base local y vuelve a crearla:
  `docker compose down -v` y luego `docker compose up -d`.

