package io.shehzeb.platform.product.service;

import io.shehzeb.platform.product.api.ProductRequest;
import io.shehzeb.platform.product.api.ProductResponse;
import io.shehzeb.platform.product.domain.Product;
import io.shehzeb.platform.product.domain.ProductNotFoundException;
import io.shehzeb.platform.product.domain.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service encapsulating product use-cases. Write operations run in read-write
 * transactions; reads run in read-only transactions to allow the persistence provider to
 * optimize flushing and locking.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product(
                request.name(), request.description(), request.price(), request.stockQuantity());
        return ProductResponse.from(repository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID id) {
        return repository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.update(request.name(), request.description(), request.price(), request.stockQuantity());
        return ProductResponse.from(repository.save(product));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
