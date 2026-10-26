# Cloud-Native Microservices Platform

A production-style microservices platform built with **Java 17** and **Spring Boot 3.3**, demonstrating
senior-level backend architecture: an API gateway, two independently deployable domain services,
OAuth2/JWT security, PostgreSQL persistence with Flyway migrations, container images, Kubernetes
manifests, and a CI pipeline with real integration tests.

> Author: **Muhammad Shehzeb Assad** — Senior Full Stack Java Developer
> Part of a portfolio of showcase projects: https://github.com/sassad25

---

## Architecture

```
                         ┌─────────────────────────┐
   client  ───────────▶  │      api-gateway        │  (Spring Cloud Gateway, :8080)
                         │  routing / edge concerns │
                         └────────────┬─────────────┘
                        /api/v1/products  \  /api/v1/orders
                             ▼                     ▼
              ┌───────────────────────┐  ┌───────────────────────┐
              │   product-service     │  │    order-service      │
              │ Spring Boot (:8081)   │  │ Spring Boot (:8082)   │
              │ JPA + Flyway          │  │ JPA + Flyway          │
              │ OAuth2 resource srv   │  │ OAuth2 resource srv   │
              └──────────┬────────────┘  └──────────┬────────────┘
                         ▼                           ▼
                 ┌──────────────┐            ┌──────────────┐
                 │ products DB  │            │  orders DB   │
                 │ PostgreSQL   │            │  PostgreSQL  │
                 └──────────────┘            └──────────────┘
```

Each service owns its own database (database-per-service), communicates over HTTP through the
gateway, and validates JWT access tokens independently as an OAuth2 resource server.

## Modules

| Module            | Port | Responsibility                                                        |
|-------------------|------|-----------------------------------------------------------------------|
| `api-gateway`     | 8080 | Single entry point; routes `/api/v1/products/**` and `/api/v1/orders/**` |
| `product-service` | 8081 | Product catalog CRUD with optimistic locking and auditing             |
| `order-service`   | 8082 | Order aggregate (orders + line items), total calculation, lifecycle   |

## Key engineering decisions

- **Spring Security as an OAuth2 Resource Server** — services are stateless and validate JWTs;
  fine-grained authorization is enforced per endpoint via `@PreAuthorize` and OAuth2 scopes
  (`products:read`, `products:write`, `orders:read`, `orders:write`).
- **RFC 7807 Problem Details** — all errors (validation, not-found, conflict) return a consistent
  `application/problem+json` contract via `@RestControllerAdvice`.
- **Optimistic locking** (`@Version`) on aggregates to safely handle concurrent updates.
- **Flyway migrations** — schema is versioned and validated (`ddl-auto: validate`); no runtime
  schema generation.
- **Database-per-service** — independent PostgreSQL instances preserve service autonomy.
- **Testcontainers integration tests** — tests run against a real PostgreSQL 16 container, not an
  in-memory substitute, so persistence behavior is verified realistically.
- **Container-first** — multi-stage Dockerfiles produce slim, non-root JRE images; Kubernetes
  manifests include readiness/liveness probes and resource requests/limits.
- **Observability** — Spring Boot Actuator with Prometheus metrics exposed on each service.

## Tech stack

Java 17 · Spring Boot 3.3 · Spring Cloud Gateway 2023.0 · Spring Security (OAuth2 Resource Server) ·
Spring Data JPA / Hibernate · PostgreSQL · Flyway · Micrometer/Prometheus · Testcontainers · JUnit 5 ·
Docker · Kubernetes · GitHub Actions.

## Running the tests

Integration tests use Testcontainers, so **Docker must be running**.

```bash
mvn clean verify
```

This compiles all modules and runs:
- `product-service` — 5 integration tests (CRUD, validation, 404, authz, unauthorized)
- `order-service` — 4 integration tests (order total, lifecycle, validation, authz)
- `api-gateway` — context/route load test

## Running locally with Docker Compose

```bash
docker compose up --build
```

This starts both PostgreSQL databases, all three services, and the gateway. The gateway is
available at `http://localhost:8080`.

> The services expect a JWT issuer at `OAUTH2_ISSUER_URI`. Point this at any OIDC provider (e.g.
> the companion [secure-auth-service](https://github.com/sassad25)), or supply tokens from your
> identity provider of choice.

## Example requests

```bash
# Create a product (requires a token with scope products:write)
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Mechanical Keyboard","description":"Hot-swappable","price":129.99,"stockQuantity":42}'

# Place an order (scope orders:write)
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-1","lines":[{"productId":"<uuid>","quantity":2,"unitPrice":10.00}]}'
```

## Deploying to Kubernetes

```bash
# Build & push images (example with GHCR)
docker build -t ghcr.io/sassad25/product-service:latest -f product-service/Dockerfile .
docker build -t ghcr.io/sassad25/order-service:latest   -f order-service/Dockerfile .
docker build -t ghcr.io/sassad25/api-gateway:latest      -f api-gateway/Dockerfile .

# Create DB secrets (example)
kubectl create secret generic product-db \
  --from-literal=url=jdbc:postgresql://<host>:5432/products \
  --from-literal=username=products --from-literal=password=<pw>

kubectl apply -f k8s/
```

## Project layout

```
cloud-native-platform/
├── api-gateway/              # Spring Cloud Gateway
├── product-service/          # Product catalog microservice
├── order-service/            # Order microservice
├── k8s/                      # Kubernetes manifests (deployments, services, probes)
├── docker-compose.yml        # Local full-stack run
├── .github/workflows/ci.yml  # CI: build + Testcontainers integration tests
└── pom.xml                   # Multi-module parent
```

## License

MIT © Muhammad Shehzeb Assad
