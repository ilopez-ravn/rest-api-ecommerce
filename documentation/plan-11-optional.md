# Plan 11: Optional Features (Extra Points)

## Goal
Add optional modules for extra credit and advanced functionality.

## Scope
- GraphQL resolve fields
- Deployment
- Refund and return system
- Auction system with concurrency handling
- Recommendation system

## Inputs
- Optional features: [documentation/requirements.md](documentation/requirements.md)

## 11.1 GraphQL Resolve Fields

### Tasks
- Implement field resolvers for nested objects to reduce over-fetching.
- Use data loaders or batch queries to avoid N+1 queries.

### Example Artifacts
- Resolver: `OrderResolver.client(orderId)` -> fetch client data
- Resolver: `Order.items(orderId)` -> fetch line items

### Acceptance Criteria
- Nested fields resolve without N+1 issues.
- Resolver behavior matches schema requirements.

## 11.2 Deployment

### Tasks
- Add deployment instructions to README.
- Configure environment variables for the target platform.
- Optional: Add Dockerfile and compose for local deploy parity.

### Example Artifacts
- README section: "Deployment (Render)"
- Dockerfile and docker-compose.yml

### Acceptance Criteria
- App deploys successfully with documented steps.
- Env vars are validated at startup.

## 11.3 Refund and Return System

### Tasks
- Add refund request entity and endpoints.
- Add manager approval flow and status tracking.
- Update inventory on product return.
- Send email notifications for each step.

### Example Artifacts
- REST: POST `/refunds` (request)
- REST: PATCH `/refunds/{id}` (approve/deny)
- Email templates: `refund-requested.html`, `refund-approved.html`

### Acceptance Criteria
- Refund flow follows the required steps.
- Inventory updates on product received.

## 11.4 Auction System with Concurrency Handling

### Tasks
- Add auction fields to products and bid entity.
- Implement optimistic or pessimistic locking on bids.
- Enforce auction end time.
- Optional: real-time bid updates via GraphQL subscriptions.

### Example Artifacts
- REST/GraphQL: `placeBid(productId, amount)`
- Concurrency: `@Version` on auction entity

### Acceptance Criteria
- Concurrent bids are handled safely.
- Auction end time is enforced.

## 11.5 Recommendation System

### Tasks
- Track user behavior (likes, views).
- Implement recommendation queries.
- Expose recommendations in product details or homepage.

### Example Artifacts
- REST: GET `/products/{id}/recommendations`
- Logic: "users who liked X also liked Y"

### Acceptance Criteria
- Recommendations are generated and returned.
- Logic uses user behavior signals.
