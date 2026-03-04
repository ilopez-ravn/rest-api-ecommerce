# Plan 06: Orders, Delivery, and Billing

## Goal
Complete ordering workflow with delivery tracking and billing.

## Scope
- Order creation and retrieval
- Delivery status and tracking
- Billing and order details

## Inputs
- GraphQL schema: [ecommerce/src/main/resources/graphql/Sales/orderSchema.graphqls](ecommerce/src/main/resources/graphql/Sales/orderSchema.graphqls)

## Tasks
- Create orders from carts with atomic stock reduction.
- Create order details and billing records.
- Implement delivery tracking and status transitions.
- Add delivery history log entries.

## Example Artifacts
- PUT `/orders` (create order)
- POST `/orders/{orderId}/shipping` (update status)
- Status transition rules: PENDING -> SHIPPED -> DELIVERED

## Acceptance Criteria
- Order creation is transactional.
- Delivery status changes are validated and logged.
- Order detail includes totals and taxes.
