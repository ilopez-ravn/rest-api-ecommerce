# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.5.10 REST API for an e-commerce platform, using Java 21 and PostgreSQL. Base URL: `http://localhost:8080/api/v1`.

## Common Commands

All Maven commands must be run from the `ecommerce/` subdirectory:

```bash
cd ecommerce

# Run the application
mvn spring-boot:run

# Build
mvn clean install

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=EcommerceApplicationTests

# Run a specific test method
mvn test -Dtest=ClassName#methodName
```

## Prerequisites

- PostgreSQL running on `localhost:5432` with database `ecommerce`, user `postgres`, password `admin`
- Database schema: apply `migrations/schema.sql`, then optionally `migrations/seed.sql`
- Configuration is in `ecommerce/src/main/resources/application.properties` (credentials are hardcoded — not env-var driven)

## Architecture

Strict layered architecture: **Controller → Service → Repository → Entity**

```
co.ravn.ecommerce/
├── Config/          # SecurityConfig, GraphQLConfig, EnvValidationConfig
├── Controllers/     # REST endpoints, grouped by domain (Auth, Inventory, Cart, Order, Clients)
├── DTO/             # Request and Response objects (no entity exposure in API)
│   ├── Request/
│   └── Response/
├── Entities/        # JPA entities, mirroring DB schema
├── Exception/       # GlobalExceptionHandler (@RestControllerAdvice)
├── Filters/         # JwtAuthFilter, XSSFilter, XSSRequestWrapper
├── Mappers/         # MapStruct interfaces (Auth, Cart, Inventory, Order); use `uses = {}` to delegate
├── Models/          # Non-entity models (e.g., GraphQL pagination types)
├── Repositories/    # Spring Data JPA interfaces
├── Resolver/        # GraphQL query/mutation resolvers
├── Services/        # Business logic
└── Utils/
    ├── enums/       # RoleEnum, EmailStatusEnum, ShoppingCartStatusEnum, etc.
    └── listener/    # ResetPasswordListener (application events)
```

Subdomains follow the same grouping (e.g., `Inventory/`, `Cart/`, `Auth/` inside each layer).

## Security & Authentication

- **Stateless JWT**: `JwtAuthFilter` validates tokens before `UsernamePasswordAuthenticationFilter`.
- **Roles**: `MANAGER`, `CLIENT`, `WAREHOUSE`, `SHIPPING` (stored in `RoleEnum`).
- Endpoint authorization is configured in `SecurityConfig.java`. Public endpoints are explicitly listed there; everything else requires authentication.
- **XSS**: `XSSFilter` (Order=1) sanitizes all incoming request parameters/bodies via `XSSRequestWrapper`.
- Passwords are BCrypt-encoded. Refresh tokens are persisted in DB (`UserRefreshToken` entity).

## Key Domain Entities

| Domain | Entities |
|--------|----------|
| Auth | `Person`, `SysUser`, `Role`, `UserRefreshToken`, `PasswordRecoveryToken` |
| Inventory | `Product`, `Category`, `Tag`, `ProductStock`, `Warehouse`, `ProductImage`, `ProductChangesLog` |
| Cart | `ShoppingCart`, `ShoppingCartDetails`, `ProductLiked` |
| Orders | `SaleOrder`, `OrderDetails`, `OrderBill`, `DeliveryTracking`, `Carrier` |
| Payments | `StripePayment`, `StripePaymentEventLog` |
| Misc | `Email`, `ClientAddress` |

`Person` ↔ `SysUser` (1:1). `Product` ↔ `Category` and `Product` ↔ `Tag` are M:N.

## Product Listing & Pagination

Products use **cursor-based pagination** (not offset). Key query params: `cursor`, `limit`, `sort_by`, `sort_order`, `categories_id`, `tags_id`, `min_price`, `max_price`, `available`, `is_active`. The response type is `ProductCursorPage` containing a list of `ProductEdge`.

## GraphQL

GraphQL is available alongside REST. Schemas live in `ecommerce/src/main/resources/graphql/` (one `.graphqls` per domain). GraphiQL IDE is enabled at `http://localhost:8080/graphiql`. Resolvers are in the `Resolver/` package.

## Cross-Cutting Concerns

- **Global exception handling**: `GlobalExceptionHandler` in `Exception/` — add new exception mappings here.
- **Email events**: Password recovery and product-liked alerts use Spring `ApplicationEvent` / `ApplicationListener` (`ResetPasswordListener`).
- **Stripe webhooks**: Handled at `/api/v1/stripe/webhook` (public endpoint).

## Conventions

- Entity packages use PascalCase subdirectories (e.g., `Entities/Cart/`, `Entities/Inventory/`).
- DTOs are strictly separated from entities; controllers receive/return DTOs only.
- Use MapStruct to map DTOs with entities in services.
- **MapStruct: use mappers inside other mappers when available.** Prefer delegating to existing mappers via `uses = { OtherMapper.class }` instead of duplicating mapping logic. Examples: `OrderMapper` uses `BillingInfoMapper`, `AddressMapper`, `ClientInfoMapper`, `WarehouseInfoMapper`, `StripePaymentMapper`, `DeliveryTrackingMapper`, `TrackingLogMapper`, `OrderItemMapper`; `ShippingDetailsMapper` uses `DeliveryTrackingMapper`, `AddressMapper`, `TrackingLogMapper`; `ProductMapper` uses `CategoryMapper`, `TagMapper`, `ProductImageMapper`; `CartMapper` uses `ProductMapper` for nested `ProductResponse`.
- For autowiring use the constructor injection with @AllArgsConstructor from lombok
- Enums used as JPA column types (e.g., `@Enumerated(EnumType.STRING)`).
- Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, etc.) is used across entities and DTOs.
- **Exceptions:** Do not throw `RuntimeException` directly. Use domain-specific exceptions so `GlobalExceptionHandler` can map to the correct HTTP status: `ResourceNotFoundException` (404), `BadRequestException` (400), `ConflictException` (409), `PaymentFailureException` (502), `ConfigurationException` (500), `InternalServiceException` (500). Add new exception types in `Exception/` and register a handler in `GlobalExceptionHandler` when needed.
