package com.busgo.trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(ScheduleIntegrationTest.TestJwtDecoderConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScheduleIntegrationTest {

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
    public void testScheduleShortWeekdayCreatesInstanceAndSeats() throws Exception {
        // create seat layout
        Map<String,Object> layout = Map.of("name","test-layout","format","json","content","{\"seats\":[{\"seatCode\":\"S1\"},{\"seatCode\":\"S2\"}]}"
        );
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
        Map<String,Object> bus = Map.of("model","TestBus","registrationNumber","TB-123","seatLayoutId", layoutId, "totalSeats", 2);
        String busJson = objectMapper.writeValueAsString(bus);

        MvcResult busRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/buses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(busJson)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map busResp = objectMapper.readValue(busRes.getResponse().getContentAsString(), Map.class);
        String busId = busResp.get("id").toString();

        // create route
        Map<String,Object> route = Map.of("code","R-TEST","origin","O","destination","D","stops","[]");
        String routeJson = objectMapper.writeValueAsString(route);

        MvcResult routeRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map routeResp = objectMapper.readValue(routeRes.getResponse().getContentAsString(), Map.class);
        String routeId = routeResp.get("id").toString();

        // create template
        Map<String,Object> template = Map.of("name","T-1","routeId", routeId, "busId", busId, "basePrice", 100);
        String templateJson = objectMapper.writeValueAsString(template);

        MvcResult templateRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(templateJson)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map templateResp = objectMapper.readValue(templateRes.getResponse().getContentAsString(), Map.class);
        String templateId = templateResp.get("id").toString();

        // prepare schedule with short weekday (today's short code)
        LocalDate today = LocalDate.now();
        String shortDow = today.getDayOfWeek().name().substring(0,3); // MON, TUE ...

        Map<String,Object> rule = Map.of(
                "templateId", templateId,
                "ruleType", "WEEKLY",
                "startDate", today.toString(),
                "endDate", today.toString(),
                "weekdays", shortDow,
                "timeOfDay", "08:30",
                "timezone", "UTC"
        );
        String ruleJson = objectMapper.writeValueAsString(rule);

        MvcResult ruleRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ruleJson)
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map ruleResp = objectMapper.readValue(ruleRes.getResponse().getContentAsString(), Map.class);
        String ruleId = ruleResp.get("id").toString();

        // expand rule for today
        MvcResult expandRes = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/schedules/"+ruleId+"/expand?start="+today.toString()+"&end="+today.toString())
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        Map expandResp = objectMapper.readValue(expandRes.getResponse().getContentAsString(), Map.class);
        Integer createdCount = (Integer) expandResp.get("createdCount");
        Assertions.assertTrue(createdCount >= 1, "expected at least 1 instance created");

        Object created = expandResp.get("created");
        // created is an array of UUIDs; pick first
        java.util.List<String> createdList = (java.util.List<String>) created;
        String instanceId = createdList.get(0);

        // fetch seats for the instance
        MvcResult seatsRes = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trips/"+instanceId+"/seats")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andReturn();

        String seatsBody = seatsRes.getResponse().getContentAsString();
        Object[] seats = objectMapper.readValue(seatsBody, Object[].class);
        Assertions.assertEquals(2, seats.length);
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
                    return new Jwt(token, java.time.Instant.now(), java.time.Instant.now().plusSeconds(3600), Map.of("alg","none"), claims);
                }
                if (token.equals("user-token")) {
                    Map<String,Object> claims = Map.of(
                            "uid", "00000000-0000-0000-0000-000000000002",
                            "roles", new String[]{"USER"}
                    );
                    return new Jwt(token, java.time.Instant.now(), java.time.Instant.now().plusSeconds(3600), Map.of("alg","none"), claims);
                }
                throw new BadJwtException("unsupported test token");
            };
        }
    }
}
