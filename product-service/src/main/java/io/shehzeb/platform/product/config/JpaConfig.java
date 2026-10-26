package io.shehzeb.platform.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate} / {@code @LastModifiedDate} on entities
 * are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
