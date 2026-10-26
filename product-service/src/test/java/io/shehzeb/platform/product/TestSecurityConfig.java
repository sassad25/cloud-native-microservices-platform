package io.shehzeb.platform.product;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;

/**
 * Supplies a stub {@link JwtDecoder} for web-layer tests so the OAuth2 resource server
 * can be exercised without a live issuer. Actual token content is driven per-request by
 * the {@code jwt()} request post-processor from spring-security-test.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .subject("test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("scope", "products:read products:write")
                .build();
    }
}
