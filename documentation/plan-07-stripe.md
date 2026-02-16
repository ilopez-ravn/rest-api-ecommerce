# Plan 07: Stripe Payments

## Goal
Integrate Stripe payment intent and webhook handling.

## Scope
- Create payment intents
- Webhook signature validation
- Idempotent event handling

## Inputs
- Stripe endpoints in OpenAPI: [documentation/open-api.spec.yml](documentation/open-api.spec.yml)

## Tasks
- Create payment intent with amount and currency.
- Persist Stripe payment details.
- Validate webhook signature.
- Process events idempotently and update order status.

## Example Artifacts
- PUT `/stripe/payment` request: `cartId`, `amount`, `currency`
- POST `/stripe/webhook` with Stripe-Signature header

## Acceptance Criteria
- Card data is never stored.
- Webhook events are handled once.
- Order status reflects payment result.
