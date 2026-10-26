package io.shehzeb.platform.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data repository for {@link Product}. Paging and sorting are inherited from
 * {@link JpaRepository} to support efficient catalog browsing.
 */
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
