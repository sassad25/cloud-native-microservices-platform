package io.shehzeb.platform.order.service;

import io.shehzeb.platform.order.api.CreateOrderRequest;
import io.shehzeb.platform.order.api.OrderResponse;
import io.shehzeb.platform.order.domain.Order;
import io.shehzeb.platform.order.domain.OrderNotFoundException;
import io.shehzeb.platform.order.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for the order bounded context. Order creation assembles the aggregate
 * and persists it atomically; the total is computed inside the aggregate.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public OrderResponse place(CreateOrderRequest request) {
        Order order = new Order(request.customerId());
        request.lines().forEach(l -> order.addLine(l.productId(), l.quantity(), l.unitPrice()));
        return OrderResponse.from(repository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return repository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> byCustomer(String customerId) {
        return repository.findByCustomerId(customerId).stream().map(OrderResponse::from).toList();
    }

    public OrderResponse pay(UUID id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.markPaid();
        return OrderResponse.from(repository.save(order));
    }
}
