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
@Import(AdminEntitiesIntegrationTest.TestJwtDecoderConfig.class)
public class AdminEntitiesIntegrationTest {

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
    public void testNonAdminCannotCreateBusRouteTrip() throws Exception {
        Map<String,Object> bus = Map.of("model","TestBus","registrationNumber","TB-001","seatLayoutId", "11111111-1111-1111-1111-111111111111", "totalSeats", 4);
        String busJson = objectMapper.writeValueAsString(bus);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/buses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(busJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/buses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(busJson)
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());

        Map<String,Object> route = Map.of("code","R1","origin","CityA","destination","CityB","stops", "[]");
        String routeJson = objectMapper.writeValueAsString(route);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson)
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());

        Map<String,Object> trip = Map.of("busId","aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa","routeId","11111111-1111-1111-1111-111111111111","basePrice",100);
        String tripJson = objectMapper.writeValueAsString(trip);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tripJson))
                .andExpect(status().isForbidden());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tripJson)
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAdminCanCreateBusRouteTripAndTripSeatsCreated() throws Exception {
        // create seat layout
        Map<String,Object> layout = Map.of("name","test-layout","format","json","content","{\"seats\": [{\"seatCode\":\"S1\"},{\"seatCode\":\"S2\"},{\"seatCode\":\"S3\"}]}" );
        String layoutJson = objectMapper.writeValueAsString(layout);

        MvcResult layoutRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/seat-layouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(layoutJson)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map layoutResp = objectMapper.readValue(layoutRes.getResponse().getContentAsString(), Map.class);
        String layoutId = layoutResp.get("id").toString();

        // create bus
        Map<String,Object> bus = Map.of("model","TestBus","registrationNumber","TB-123","seatLayoutId", layoutId, "totalSeats", 3);
        String busJson = objectMapper.writeValueAsString(bus);

        MvcResult busRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/buses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(busJson)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map busResp = objectMapper.readValue(busRes.getResponse().getContentAsString(), Map.class);
        String busId = busResp.get("id").toString();
        Assertions.assertEquals("00000000-0000-0000-0000-000000000001", busResp.get("createdBy").toString());

        // create route
        Map<String,Object> route = Map.of("code","R-TEST","origin","O","destination","D","stops", "[]");
        String routeJson = objectMapper.writeValueAsString(route);

        MvcResult routeRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map routeResp = objectMapper.readValue(routeRes.getResponse().getContentAsString(), Map.class);
        String routeId = routeResp.get("id").toString();
        Assertions.assertEquals("00000000-0000-0000-0000-000000000001", routeResp.get("createdBy").toString());

        // create trip
        Map<String,Object> trip = Map.of("busId", busId, "routeId", routeId, "basePrice", 150);
        String tripJson = objectMapper.writeValueAsString(trip);

        MvcResult tripRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tripJson)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map tripResp = objectMapper.readValue(tripRes.getResponse().getContentAsString(), Map.class);
        String tripId = tripResp.get("tripId").toString();

        // verify trip seats were generated
        MvcResult seatsRes = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trips/"+tripId+"/seats"))
                .andExpect(status().isOk())
                .andReturn();

        String seatsBody = seatsRes.getResponse().getContentAsString();
        Object[] seats = objectMapper.readValue(seatsBody, Object[].class);
        Assertions.assertEquals(3, seats.length);
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
