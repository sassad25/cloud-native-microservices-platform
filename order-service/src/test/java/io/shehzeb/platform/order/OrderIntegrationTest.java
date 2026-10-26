package io.shehzeb.platform.order;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the order service against real PostgreSQL (Testcontainers).
 * Verifies aggregate total calculation, order lifecycle transitions, validation, and auth.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(TestSecurityConfig.class)
class OrderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    private static org.springframework.test.web.servlet.request.RequestPostProcessor write() {
        return jwt().jwt(j -> j.claim("scope", "orders:write"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor read() {
        return jwt().jwt(j -> j.claim("scope", "orders:read"));
    }

    @Test
    void placeOrder_computesTotal_andCanBePaid() throws Exception {
        String payload = """
                {
                  "customerId": "cust-1",
                  "lines": [
                    {"productId": "11111111-1111-1111-1111-111111111111", "quantity": 2, "unitPrice": 10.00},
                    {"productId": "22222222-2222-2222-2222-222222222222", "quantity": 1, "unitPrice": 5.50}
                  ]
                }
                """;

        String location = mockMvc.perform(post("/api/v1/orders")
                        .with(write())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.total").value(25.50))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/payment").with(write()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void placeOrder_withNoLines_isBadRequest() throws Exception {
        String payload = """
                {"customerId": "cust-2", "lines": []}
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .with(write())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"));
    }

    @Test
    void getOrders_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders?customerId=cust-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void payUnknownOrder_returnsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/orders/00000000-0000-0000-0000-000000000000/payment").with(write()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"));
    }
}
