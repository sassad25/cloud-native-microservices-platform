package io.shehzeb.platform.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Order microservice. Persists customer orders and their line items, secured as an
 * OAuth2 resource server. Demonstrates a second bounded context behind the shared gateway.
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
