# Plan 09: GraphQL Parity and Resolvers

## Goal
Match GraphQL schemas with working resolvers and auth rules.

## Scope
- Missing queries and mutations
- Auth parity with REST
- Nested field resolvers

## Inputs
- GraphQL schemas: [ecommerce/src/main/resources/graphql](ecommerce/src/main/resources/graphql)

## Tasks
- Implement resolvers for orders, carts, payments, users, delivery.
- Add auth checks consistent with REST rules.
- Add field resolvers for nested objects.

## Example Artifacts
- Query: `orders(page: 1)` -> returns order list
- Mutation: `updateDeliveryStatus(orderId, status)`

## Acceptance Criteria
- All schemas have matching resolvers.
- Unauthorized queries are blocked.
- Nested fields resolve without N+1 issues.
