package cl.knowsis.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración de la seguridad. Usan los usuarios de ejemplo de la migración V20
 * (contraseña de todos: Knows#2026), así que necesitan MySQL corriendo (docker compose up -d).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.jwt.secret=clave-de-pruebas-con-mas-de-32-caracteres-ok")
class AuthIntegrationTest {

    private static final String CLAVE = "Knows#2026";

    @Autowired
    private MockMvc mvc;

    /** Inicia sesión y devuelve el accessToken. */
    private String tokenDe(String correo, String contrasena) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.accessToken");
    }

    @Test
    void loginCorrectoDevuelveTokensYElUsuario() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"kevin@knowsis.cl\",\"contrasena\":\"" + CLAVE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("Admin"))
                .andExpect(jsonPath("$.usuario.contrasena").doesNotExist());
    }

    @Test
    void loginConClaveMalaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"kevin@knowsis.cl\",\"contrasena\":\"claveMala1#\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void endpointDeAdminSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    void endpointDeAdminConTokenDeAlumnoDevuelve403() throws Exception {
        String token = tokenDe("alumno@knowsis.cl", CLAVE);

        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403));
    }

    @Test
    void endpointDeAdminConTokenDeAdminDevuelve200() throws Exception {
        String token = tokenDe("kevin@knowsis.cl", CLAVE);

        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").exists());
    }
}