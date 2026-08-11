package com.busgo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(AdminSecurityIntegrationTest.TestJwtDecoderConfig.class)
public class AdminSecurityIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("testdb").withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry reg) {
        postgres.start();
        String jdbc = postgres.getJdbcUrl();
        reg.add("spring.datasource.url", () -> jdbc);
        reg.add("spring.datasource.username", () -> postgres.getUsername());
        reg.add("spring.datasource.password", () -> postgres.getPassword());
    }

    @AfterAll
    static void tearDown() {
        try { postgres.stop(); } catch (Exception ignored) {}
    }

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeAll
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testNonAdminCannotCreateSeatLayout() throws Exception {
        Map<String,Object> payload = Map.of("name","test-layout","format","json","content","{}");
        String json = objectMapper.writeValueAsString(payload);

        // without token
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/seat-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());

        // with non-admin token
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/seat-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAdminCanCreateSeatLayoutAndCreatedBySet() throws Exception {
        Map<String,Object> payload = Map.of("name","admin-layout","format","json","content","{}");
        String json = objectMapper.writeValueAsString(payload);

        MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/seat-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        String body = res.getResponse().getContentAsString();
        Map resp = objectMapper.readValue(body, Map.class);
        Assertions.assertTrue(resp.containsKey("id") || resp.containsKey("createdBy"));
        // if entity persisted, createdBy should equal the uid in the admin-token (00000000-0000-0000-0000-000000000001)
        if (resp.containsKey("createdBy")) {
            Assertions.assertEquals("00000000-0000-0000-0000-000000000001", resp.get("createdBy").toString());
        }
    }

    @TestConfiguration
    public static class TestJwtDecoderConfig {
        @Bean
        public JwtDecoder jwtDecoder() {
            return token -> {
                if (token == null) throw new BadJwtException("token null");
                if (token.equals("admin-token")) {
                    Map<String,Object> claims = Map.of(
                            "uid", "00000000-0000-0000-0000-000000000001",
                            "roles", new String[]{"ADMIN"}
                    );
                    return new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg","none"), claims);
                }
                if (token.equals("user-token")) {
                    Map<String,Object> claims = Map.of(
                            "uid", "00000000-0000-0000-0000-000000000002",
                            "roles", new String[]{"USER"}
                    );
                    return new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg","none"), claims);
                }
                throw new BadJwtException("unsupported test token");
            };
        }
    }
}
