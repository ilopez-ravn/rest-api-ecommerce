# Plan 01: Security Foundation

## Goal
Establish authentication, authorization, and platform security defaults.

## Scope
- Public vs protected routes
- JWT verification and refresh token flow
- Role-based access rules
- CORS and CSRF
- Rate limiting for password reset

## Inputs
- Security config: [ecommerce/src/main/java/co/ravn/ecommerce/Config/SecurityConfig.java](ecommerce/src/main/java/co/ravn/ecommerce/Config/SecurityConfig.java)
- JWT filter: [ecommerce/src/main/java/co/ravn/ecommerce/Filters/JwtAuthFilter.java](ecommerce/src/main/java/co/ravn/ecommerce/Filters/JwtAuthFilter.java)

## Tasks
[x] Define public endpoints: products, categories, tags.
[x] Protect carts, orders, likes, payments.
[ ] Enforce role checks for manager-only endpoints.
[x] Configure CORS for approved origins.
[ ] Add rate limiting for password reset endpoints.

## Example Artifacts
- Route table: `/products` public, `/orders` auth required
- JWT claims: `sub`, `role`, `exp`
- Rate limit: 5 requests per 10 minutes for reset token

## Acceptance Criteria
- Unauthorized access returns 401/403 correctly.
- CORS works for allowed origins only.
- Rate limit triggers and logs correctly.
