package com.example.employeesearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end coverage of every endpoint (A1, A2, E1, E2, M1) plus security, running against a
 * throwaway SQLite DB built from the legacy DDL with {@code ddl-auto=validate} — also proving
 * the JPA mapping validates against the real schema (C1).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class EmployeeSearchIntegrationTest {

    private static Path dbFile;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @BeforeAll
    static void buildDb() throws Exception {
        dbFile = TestSqliteDb.createSeeded();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + dbFile.toAbsolutePath());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.community.dialect.SQLiteDialect");
        registry.add("app.jwt.secret", () -> "integration-test-secret-integration-test-secret-0123456789");
        registry.add("app.jwt.expiry-minutes", () -> "60");
        registry.add("app.cors.allowed-origins", () -> "http://localhost:4200");
    }

    private String loginAndGetToken() throws Exception {
        MvcResult res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return json.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void login_success_returnsToken() throws Exception {
        assertThat(loginAndGetToken()).isNotBlank();
    }

    @Test
    void login_blankField_returns400WithMessage() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Please enter both username and password."));
    }

    @Test
    void login_badCredentials_returns401WithGenericMessage() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password. Please try again."));
    }

    @Test
    void search_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void search_noFilters_returnsAll15SortedByName() throws Exception {
        String token = loginAndGetToken();
        mvc.perform(get("/api/employees").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(15))
                .andExpect(jsonPath("$[0].name").value("Alice Johnson"))
                .andExpect(jsonPath("$[14].name").value("Olivia Chen"))
                .andExpect(jsonPath("$[0].salary").value(85000.0));
    }

    @Test
    void search_byDepartmentAndStatus_appliesAndPredicate() throws Exception {
        String token = loginAndGetToken();
        mvc.perform(get("/api/employees?department=IT&status=Active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                // IT + Active: Alice, Eva, Irene (Liam is On Leave)
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void search_allValueIsIgnored() throws Exception {
        String token = loginAndGetToken();
        mvc.perform(get("/api/employees?department=All&role=All&status=All")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(15));
    }

    @Test
    void search_byNamePartial_caseInsensitiveAsciiLike() throws Exception {
        String token = loginAndGetToken();
        // "smith" lower-case matches "Bob Smith" — SQLite LIKE is ASCII case-insensitive (BR-4/R-2).
        mvc.perform(get("/api/employees?name=smith")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bob Smith"));
    }

    @Test
    void export_returnsCsvWithDispositionAndRawSalary() throws Exception {
        String token = loginAndGetToken();
        MvcResult res = mvc.perform(get("/api/employees/export?department=IT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.matchesPattern(".*filename=\"employees_\\d{8}_\\d{6}\\.csv\".*")))
                .andReturn();

        String body = res.getResponse().getContentAsString();
        assertThat(body).startsWith("Id,Name,Department,Role,Status,Email,Phone,HireDate,Salary\r\n");
        assertThat(body).contains("Alice Johnson,IT,Developer,Active,alice@corp.com,555-0101,2021-03-15,85000\r\n");
        // No BOM
        assertThat(res.getResponse().getContentAsByteArray()[0]).isNotEqualTo((byte) 0xEF);
    }

    @Test
    void export_emptyResult_returns409() throws Exception {
        String token = loginAndGetToken();
        mvc.perform(get("/api/employees/export?name=zzz-no-match")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("There are no rows to export."));
    }

    @Test
    void meta_filters_returnsAllPrefixedDomains() throws Exception {
        String token = loginAndGetToken();
        MvcResult res = mvc.perform(get("/api/meta/filters")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = json.readTree(res.getResponse().getContentAsString());
        assertThat(node.get("departments").get(0).asText()).isEqualTo("All");
        assertThat(node.get("roles").get(0).asText()).isEqualTo("All");
        assertThat(node.get("statuses").get(0).asText()).isEqualTo("All");
        assertThat(node.get("statuses").toString()).contains("On Leave");
    }
}
