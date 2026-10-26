package io.shehzeb.platform.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Product catalog microservice.
 *
 * <p>Exposes a REST API for managing products, secured as an OAuth2 resource server
 * (validates JWT access tokens). Persistence is backed by PostgreSQL via Spring Data JPA
 * with Flyway-managed schema migrations.</p>
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
