# Implementation Plan: Spring Boot Ecommerce API

This plan defines the steps to build the REST and GraphQL API with best practices. It maps each requirement to concrete modules, endpoints, and code areas in the existing project structure.

## 0) Baseline and Project Hygiene

- Define environment configuration with validation:
  - Create a config properties class for required env vars (DB, JWT, Stripe, Mail).
  - Add startup validation (fail fast if missing or invalid).
- Add global exception handling:
  - Create a ControllerAdvice to standardize error responses and map common exceptions.
- Add request DTO validation:
  - Define request/response DTOs per endpoint and add validation annotations.
- Add one custom validation annotation:
  - Example: @StrongPassword or @ValidDocumentType.
- Confirm logging and error handling strategy:
  - Use consistent error shape and clear messages.

## 1) Security Foundation

- Configure Spring Security:
  - Define public vs protected routes (products and categories public; cart, orders, likes require auth).
  - Enable CORS configuration for frontend domains.
  - Define CSRF policy (disabled for stateless JWT) and ensure safe defaults.
- JWT and refresh tokens:
  - Validate JWT expiration and claims.
  - Implement refresh flow via DB tokens.
- Role-based authorization:
  - Add method-level annotations and/or security config for MANAGER vs CLIENT permissions.
- Rate limiting:
  - Add rate limiting filter or interceptor for password reset endpoints.

## 2) Authentication (REST)

Endpoints:
- POST /users (signup)
- POST /users/login (signin)
- POST /users/refresh (token refresh)
- POST /users/password/token (forgot password)
- PUT /users/password (reset password)
- POST /users/logout (sign out)

Tasks:
- Implement signup, login, logout with JWT and refresh tokens.
- Implement password reset token flow with expiry and rate limiting.
- Persist password recovery tokens.
- Hash passwords using BCrypt (never store plaintext).

## 3) Catalog and Public Access (REST)

Endpoints:
- GET /products (pagination, sorting, filtering)
- GET /products/{productId}
- GET /categories
- GET /tags

Tasks:
- Implement product list with pagination metadata and filters (category, name, price).
- Ensure inactive or deleted products are hidden from public view.
- Add product detail endpoint with images and stock availability.

## 4) Manager Features (REST + GraphQL)

Manager REST endpoints:
- POST /products
- PUT /products/{productId}
- PATCH /products/{productId}
- DELETE /products/{productId}
- POST /products/{productId}/image
- DELETE /products/{productId}/image/{imageId}
- POST /warehouses
- PUT /warehouses/{warehouseId}
- DELETE /warehouses/{warehouseId}
- POST /warehouses/{warehouseId}/stock

Manager GraphQL:
- Delete products, disable products, update delivery status

Tasks:
- Product CRUD with validation and logging of price changes.
- Soft delete and disable flags (prevent client purchases).
- Image upload validation (PNG/JPEG), compression and unique names.
- Warehouse and stock updates with constraints.

## 5) Client Features (REST + GraphQL)

Client REST endpoints:
- GET /clients, GET /clients/{clientId}
- POST /clients
- PUT /clients/{clientId}
- DELETE /clients/{clientId}

Cart REST endpoints:
- POST /carts
- GET /carts/{cartId}
- PUT /carts/{cartId}
- DELETE /carts/{cartId}
- POST /carts/{cartId}/items
- PATCH /carts/{cartId}/items/{itemId}
- DELETE /carts/{cartId}/items/{itemId}
- GET /carts/clients/{clientId}

Client GraphQL:
- Like products (mutation)
- Cart management (mutations)
- Order management (queries/mutations)

Tasks:
- Implement cart lifecycle, add/remove items, update quantity.
- Validate stock before cart add and before order creation.
- Implement product like/unlike and link to notifications.

## 6) Orders, Delivery, and Billing

Endpoints:
- PUT /orders (create order)
- GET /orders
- GET /orders/{orderId}
- GET /orders/status/{shoppingCartId}
- GET /orders/{orderId}/shipping
- POST /orders/{orderId}/shipping

Tasks:
- Create order from cart with atomic stock reduction.
- Create order billing and order details.
- Implement delivery status and tracking.
- Enforce valid delivery status transitions.

## 7) Stripe Payments

Endpoints:
- PUT /stripe/payment (create payment intent)
- POST /stripe/webhook (handle events)

Tasks:
- Create Stripe payment intent and persist details.
- Validate webhook signature and process events idempotently.
- Update order status based on payment result.

## 8) Email Notifications (Async)

Triggers:
- Password change notifications
- Order status updates
- Low stock alerts (liked products)
- Discount alerts (liked products)

Tasks:
- Implement async email sender (Spring async or queue).
- Templates for each email type.
- Persist email logs and failures without crashing.

## 9) GraphQL Parity and Resolvers

Tasks:
- Implement missing resolvers to match schemas in src/main/resources/graphql.
- Ensure auth rules are consistent with REST rules.
- Add field resolvers for nested objects when needed.

## 10) Testing and Verification

- Unit tests for services and validation.
- Integration tests for controllers and security.
- Tests for rate limiting and password reset.
- End-to-end tests for critical flows: signup, login, cart, checkout, payment, delivery status.

---

## Traceability Matrix (Requirement -> Endpoint/Module)

### Auth System
- Sign up -> POST /users -> AuthController/AuthService
- Sign in -> POST /users/login -> AuthController/AuthService
- Sign out -> POST /users/logout -> AuthController/AuthService
- Forgot password -> POST /users/password/token -> AuthController/PasswordResetService
- Reset password -> PUT /users/password -> AuthController/PasswordResetService

### Product Catalog
- List products -> GET /products -> ProductController/ProductService
- Search by category -> GET /products?category= -> ProductController/ProductService

### Roles
- Manager access -> security config + method-level annotations
- Client access -> security config + method-level annotations

### Email Notifications
- Password change -> EmailService + EmailLog
- Order status -> EmailService + Order service
- Low stock -> Stock watcher + EmailService
- Discount alerts -> Price change hook + EmailService

### Manager Features
- Create/update/delete products -> ProductController/ProductService
- Disable products -> ProductController/ProductService
- Upload images -> ProductImageController/ProductImageService
- Update delivery status -> DeliveryController/DeliveryService

### Client Features
- View products -> GET /products, GET /products/{id}
- Cart management -> CartController/CartService
- Like products -> ProductLikeService + GraphQL mutation
- View orders -> OrderController/OrderService
- Track delivery -> DeliveryController/DeliveryService

### Stripe Integration
- Create payment intent -> StripePaymentController/StripeService
- Webhook handling -> StripeWebhookController/StripeService

---

## Suggested File Placement

- This plan lives at documentation/implementation-plan.md
- Update README with a short pointer to this plan if desired
