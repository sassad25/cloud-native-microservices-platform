package io.shehzeb.platform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API gateway — the single entry point for the platform. Routes external traffic to the
 * product and order services, centralizing cross-cutting edge concerns (routing, timeouts,
 * and a place to add rate-limiting / tracing).
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
