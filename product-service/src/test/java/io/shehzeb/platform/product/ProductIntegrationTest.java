package io.shehzeb.platform.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.shehzeb.platform.product.api.ProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test running against a real PostgreSQL instance (Testcontainers),
 * exercising Flyway migrations, JPA persistence, Bean Validation, the RFC 7807 error contract,
 * and OAuth2 scope-based authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(TestSecurityConfig.class)
class ProductIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static org.springframework.test.web.servlet.request.RequestPostProcessor readScope() {
        return jwt().jwt(j -> j.claim("scope", "products:read"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor writeScope() {
        return jwt().jwt(j -> j.claim("scope", "products:write"));
    }

    @Test
    void createThenGet_returnsPersistedProduct() throws Exception {
        ProductRequest request = new ProductRequest("Mechanical Keyboard", "Hot-swappable", new BigDecimal("129.99"), 42);
        String body = objectMapper.writeValueAsString(request);

        String location = mockMvc.perform(post("/api/v1/products")
                        .with(writeScope())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location).with(readScope()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(42));
    }

    @Test
    void create_withInvalidPayload_returnsValidationProblem() throws Exception {
        String invalid = """
                {"name":"","price":-5,"stockQuantity":-1}
                """;

        mockMvc.perform(post("/api/v1/products")
                        .with(writeScope())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void read_withWriteOnlyScope_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(jwt().jwt(j -> j.claim("scope", "nothing:useful"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void request_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void get_missingProduct_returnsNotFoundProblem() throws Exception {
        mockMvc.perform(get("/api/v1/products/00000000-0000-0000-0000-000000000000").with(readScope()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"));
    }
}
