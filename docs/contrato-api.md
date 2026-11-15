# Contrato de la API · Knows-IS

Es el acuerdo entre backend y frontend: **si algo cambia aquí, se avisa a los tres antes de programarlo.**
Los nombres de los campos son los mismos que ya usa el frontend, para tocarlo lo mínimo.

## Reglas generales

- URL base en desarrollo: `http://localhost:8080` · todo en JSON (UTF-8).
- Fechas en ISO-8601 UTC (`2026-11-20T15:30:00Z`). Dinero en pesos chilenos, **enteros** (sin decimales).
- Autenticación: encabezado `Authorization: Bearer <accessToken>`.
- Los valores de estas listas se escriben **tal cual** en la base de datos y en el JSON. En Java usa un `enum` con los mismos nombres
  (ej. `enum Rol { Admin, Instructor, Alumno }`). Para Spring Security, la autoridad es `"ROLE_" + rol.name().toUpperCase()`.
  - `rol`: `Admin` · `Instructor` · `Alumno`
  - `estado` de usuario: `Activo` · `Inactivo`
  - `estado` de orden: `Pagada` · `Anulada`
  - `nivel` de curso: `Inicial` · `Intermedio` · `Avanzado`
- Nunca se devuelven contraseñas, hashes ni tokens guardados.

### Formato de error (siempre el mismo)

```json
{ "estado": 409, "error": "Conflict", "mensaje": "Ya existe un curso con el código 10001",
  "ruta": "/api/cursos", "fecha": "2026-11-20T15:30:00Z", "detalles": ["codigo: no puede estar vacío"] }
```

El frontend muestra `mensaje`. `detalles` solo aparece en errores de validación (400).

| Código | Cuándo |
|---|---|
| 200 / 201 / 204 | OK / creado / OK sin contenido (DELETE y logout) |
| 400 | Datos inválidos o JSON mal formado |
| 401 | Sin token, token inválido o vencido, o credenciales incorrectas |
| 403 | Token válido pero sin permiso (rol insuficiente o recurso ajeno) |
| 404 | El recurso no existe |
| 409 | Dato repetido, ya inscrito, sin cupos, curso con órdenes |
| 422 | Pago rechazado u otra regla de negocio |

## Permisos por rol

| Quién | Qué puede hacer |
|---|---|
| Sin sesión | Ver cursos y categorías · registrarse · iniciar sesión · renovar sesión |
| Alumno / Instructor | Todo lo anterior + `/api/auth/me` · comprar · ver **sus** órdenes |
| Admin | Todo + crear, editar y borrar cursos · CRUD de usuarios · ver y anular cualquier orden · resumen de cupos |

## Endpoints

### Sistema
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/ping` | Público |

### Autenticación · `/api/auth` (frente B)
| Método | Ruta | Acceso | Entrada → salida |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Datos del usuario → `201` usuario (siempre con rol `Alumno`) |
| POST | `/api/auth/login` | Público | `{correo, contrasena}` → sesión |
| POST | `/api/auth/refresh` | Público | `{refreshToken}` → sesión nueva (el refresh anterior queda inválido) |
| POST | `/api/auth/logout` | Público | `{refreshToken}` → `204` (lo revoca) |
| GET | `/api/auth/me` | Con sesión | → usuario |

Sesión (respuesta de login y refresh):
```json
{ "accessToken": "eyJ...", "refreshToken": "9f3a...", "tipo": "Bearer", "expiraEnSegundos": 900,
  "usuario": { "id": 4, "nombre": "Camila", "apellidos": "Rojas", "correo": "camila@knowsis.cl", "rol": "Alumno",
               "estado": "Activo", "telefono": "+56912345678", "region": "...", "comuna": "...", "direccion": "...", "rut": null } }
```
Registro: `{ nombre, apellidos, correo, contrasena, telefono?, region?, comuna?, direccion? }`.

### Cursos · `/api/cursos` (frente A)
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/cursos?categoria=redes&nivel=Inicial&q=java` (todos los filtros opcionales) | Público |
| GET | `/api/cursos/{id}` | Público |
| POST | `/api/cursos` | Admin |
| PUT | `/api/cursos/{id}` | Admin |
| DELETE | `/api/cursos/{id}` | Admin (409 si tiene órdenes) |
| GET | `/api/categorias` | Público |

```json
{ "id": 1, "codigo": "10001", "nombre": "JavaScript desde cero", "descripcion": "...", "precio": 210000,
  "cupos": 30, "cupoCritico": 5, "categoria": "programacion", "nivel": "Inicial", "instructor": "Kevin Soto",
  "imagen": "https://...", "icono": "bi-code-slash", "destacado": true, "nuevo": false,
  "cuposOcupados": 3, "cuposDisponibles": 27 }
```
En POST/PUT no se envían `id`, `cuposOcupados` ni `cuposDisponibles`. `categoria` es el `slug` (`programacion`, `redes`, `diseno`, `ciberseguridad`, `datos`, `ia`).
Reglas: `codigo` único (409) · `cupoCritico` ≤ `cupos` · `precio` y `cupos` ≥ 0 · `cupos` son los **totales**; `cuposOcupados` se cuenta desde las órdenes pagadas.

### Usuarios · `/api/usuarios` (frente B, solo Admin)
| Método | Ruta |
|---|---|
| GET | `/api/usuarios` |
| GET | `/api/usuarios/{id}` |
| POST | `/api/usuarios` |
| PUT | `/api/usuarios/{id}` |
| DELETE | `/api/usuarios/{id}` |

Cuerpo de POST/PUT: usuario + `contrasena` (en PUT, vacía o ausente = no cambiarla). Reglas: correo único (409) · un admin no puede eliminarse a sí mismo · un usuario `Inactivo` no puede iniciar sesión.

### Órdenes · `/api/ordenes` (frente C)
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/ordenes` | Con sesión |
| GET | `/api/ordenes/mias` | Con sesión |
| GET | `/api/ordenes/{id}` (el id es el número, ej. `KS-00001`) | Dueño o Admin |
| GET | `/api/ordenes` | Admin |
| PATCH | `/api/ordenes/{id}/anular` | Admin (libera los cupos) |
| GET | `/api/admin/cupos` | Admin |

Crear orden (el servidor ignora cualquier precio o total que mande el cliente):
```json
{ "cursoIds": [1, 3], "cuotas": 3,
  "facturacion": { "nombre": "Camila Rojas", "rut": "12.345.678-5", "correo": "camila@knowsis.cl", "telefono": "", "direccion": "Av. Siempre Viva 742" },
  "tarjeta": { "numero": "4242 4242 4242 4242", "titular": "CAMILA ROJAS", "vencimiento": "12/28", "cvv": "123" } }
```
Respuesta `201`:
```json
{ "id": "KS-00001", "usuarioId": 4, "fecha": "2026-11-20T15:30:00Z",
  "items": [{ "cursoId": 1, "codigo": "10001", "nombre": "JavaScript desde cero", "instructor": "Kevin Soto", "categoria": "programacion", "precio": 210000 }],
  "subtotal": 470000, "descuento": 31334, "total": 438666, "cuotas": 3, "primeraCuota": 125334, "valorCuota": 156666,
  "facturacion": { "nombre": "...", "rut": "...", "correo": "...", "telefono": "", "direccion": "..." },
  "pago": { "medio": "Tarjeta •••• 4242", "referencia": "PAY-LX3K9" }, "estado": "Pagada" }
```
Reglas de negocio:
- Cuotas permitidas: 1, 3 o 6, sin interés. Con 3 o 6 cuotas, la **primera cuota lleva 20 % de descuento**; el pago único no.
  `valorCuota = floor(subtotal / cuotas)`; `primeraBruta = subtotal − valorCuota × (cuotas − 1)`; `descuento = round(primeraBruta × 0.20)`;
  `total = subtotal − descuento`; `primeraCuota = primeraBruta − descuento`. (Igual que `planPago` en `src/utils/pagos.js` del frontend.)
- Pago simulado: `4242 4242 4242 4242` aprobado · `4000 0000 0000 0002` rechazado (422) · cualquier otro número válido (Luhn) aprobado.
  Se guardan solo los últimos 4 dígitos y la referencia. **Nunca** el número completo ni el CVV.
- Errores: ya inscrito → 409 · sin cupos → 409 · pago rechazado → 422 · RUT inválido → 400 · curso inexistente → 404.
- Cupos: `ocupados` = ítems de órdenes `Pagada` del curso · `disponibles` = `cupos` − `ocupados`. Una orden `Anulada` libera el cupo.

Resumen de cupos (`GET /api/admin/cupos`):
```json
{ "cuposTotales": 103, "cuposOcupados": 3, "cuposDisponibles": 100, "cursosConCupoCritico": 0,
  "porCurso": [{ "cursoId": 1, "nombre": "JavaScript desde cero", "cupos": 30, "ocupados": 3, "disponibles": 27, "critico": false }] }
```
