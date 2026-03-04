## Stripe Payment Integration – Implementation Guide

This document describes how to implement the **Stripe payment integration** required by `requirements.md` and defined in `open-api.spec.yml`.

### Goals

- Create Stripe Payment Intents for a shopping cart.
- Expose the `/stripe/payment` endpoint for creating/retrieving a Payment Intent.
- Expose the `/orders/status/{shoppingCartId}` endpoint for checking payment/order status.
- Expose the `/stripe/webhook` endpoint for Stripe webhooks (success/failure).
- Update orders and carts based on payment status.
- Verify Stripe webhook signatures and ensure idempotent processing.

---

### 1. Configuration

- **Dependencies**
  - Add Stripe Java SDK dependency to `pom.xml`.
- **Properties**
  - Add configuration keys (load values from environment):
    - `stripe.api-key` – secret API key.
    - `stripe.webhook-secret` – webhook signing secret.
    - Optionally: `stripe.currency` (e.g. `usd`).
- **Config class**
  - Create a `StripeConfig` (or similar) class:
    - Reads the properties above.
    - Exposes configured Stripe client / initializes `Stripe.apiKey`.

---

### 2. Domain & Persistence Model

- **Entities**
  - Ensure there is an `Order` entity that matches the `Order` schema in the OpenAPI:
    - Fields: `id`, `client`, `warehouse`, `orderDate`, `billingInformation`, `address`, `paymentInfo`, etc.
  - Create a `StripePayment` (or `Payment`) entity mapped to `stripePayment` schema:
    - `id`
    - `order` (ManyToOne)
    - `paymentType` (e.g. `"stripe"`)
    - `amount`
    - `currency`
    - `status` (`SUCCEEDED`, `PENDING`, `FAILED`)
    - `paymentMethod` (e.g. `"card"`)
    - `stripePaymentIntentId` (to correlate with Stripe)
    - `createdAt`, `updatedAt`.
- **Repositories**
  - Add `StripePaymentRepository` (or generic `PaymentRepository`) with:
    - `Optional<StripePayment> findByStripePaymentIntentId(String paymentIntentId);`
    - `Optional<StripePayment> findByOrderId(Integer orderId);`

- **Database schema (`migrations/schema.sql`)**
  - Add or modify tables in `migrations/schema.sql` as needed to support the Stripe entities and idempotency:
    - **Payment table** (e.g. `stripe_payment` or `payment`): columns for `id`, `order_id` (FK to order), `payment_type`, `amount`, `currency`, `status` (enum or varchar), `payment_method`, `stripe_payment_intent_id` (unique), `created_at`, `updated_at`. Add this table if it does not exist; if an existing `order` or `payment` table lacks Stripe fields, add the new columns or a dedicated Stripe payment table.
    - **Processed webhook events** (for idempotency): table such as `processed_stripe_event` with `id` (Stripe event id, primary key or unique), `processed_at` (timestamp). Add this table so the webhook handler can skip duplicate events.
  - Keep schema changes in `migrations/schema.sql` and run them (or apply via your migration process) before relying on the new entities in the application.

---

### 3. Service Layer Design

- **StripePaymentService**
  - Responsibilities:
    - Create or retrieve a Payment Intent for a cart.
    - Compute cart total (sum of line items * price).
    - Interact with Stripe SDK:
      - `PaymentIntent.create(...)`
      - `PaymentIntent.retrieve(...)` (if needed).
    - Map Stripe amounts to smallest currency unit (e.g. cents).
    - Persist/update `StripePayment` entity linked to the order/cart.
  - Methods:
    - `PaymentIntentDTO createOrRetrievePaymentIntent(Integer shoppingCartId)`
      - Load active cart by `shoppingCartId`.
      - Validate: cart belongs to current user; cart has products; stock is available.
      - Compute amount.
      - Create (or reuse) Stripe Payment Intent.
      - Store `stripePaymentIntentId` + initial status (`PENDING`).
      - Return a DTO `{ clientSecret }` per `PaymentIntent` schema.
    - `OrderDTO getOrderStatusByShoppingCartId(Integer shoppingCartId)`
      - Find order associated with cart.
      - Map to `Order` response (including `payment_info`).

- **WebhookService**
  - Responsibilities:
    - Verify Stripe signature.
    - Deserialize webhook payload to an internal representation.
    - Ensure idempotency (event `id` processed once).
    - React to:
      - `payment_intent.succeeded`
      - `payment_intent.payment_failed`
    - Update:
      - `StripePayment.status`
      - `Order` status.
      - Inventory (reduce stock on success).
      - Cart (close/clear on success).
  - Methods:
    - `void handleStripeEvent(String payload, String stripeSignature)`
      - Verify signature with `stripe.webhook-secret`.
      - Parse event type and Payment Intent `id`.
      - Check idempotency table/log.
      - Dispatch to handlers:
        - `handlePaymentSucceeded(...)`
        - `handlePaymentFailed(...)`.

---

### 4. Controllers / Endpoints

#### 4.1 `PUT /stripe/payment` – `createPaymentIntent`

- **Location**
  - Add `StripeController` (e.g. `co.ravn.ecommerce.Controllers.Payments.StripeController`).
- **Signature**
  - `@PutMapping("/stripe/payment")`
  - Request body:
    - `{ "shopping_cart_id": number }`
- **Flow**
  - Authenticate user (JWT).
  - Delegate to `StripePaymentService.createOrRetrievePaymentIntent(shoppingCartId)`.
  - On success:
    - Return `201` with `{ "client_secret": "..." }`.
  - On validation error:
    - Return `400` with `InvalidQueryResponse`.
  - On rate-limit violation (optional per requirements):
    - Return `429`.

#### 4.2 `GET /orders/status/{shoppingCartId}` – `getPaymentStatus`

- **Location**
  - In existing `OrderController` (or new controller under orders).
- **Flow**
  - Validate `shoppingCartId`.
  - Resolve order and its `StripePayment`.
  - Map to `Order` schema:
    - `payment_info` must match `stripePayment` schema (status, amount, currency, paymentMethod, etc.).
  - Return `200` or:
    - `400` if invalid ID.
    - `404` if order not found.

#### 4.3 `POST /stripe/webhook` – `handleStripeWebhook`

- **Location**
  - In `StripeController`.
- **Security**
  - Exclude from JWT authentication in `SecurityConfig`.
  - Require `stripe-signature` header.
- **Flow**
  - Read **raw** request body (needed for Stripe signature verification).
  - Read `stripe-signature` header.
  - Delegate to `WebhookService.handleStripeEvent(payload, signature)`.
  - Always return a fast `200` on successful processing.
  - On invalid signature:
    - Return `400` or `401`, log error.

---

### 5. Webhook Signature Verification & Idempotency

- **Signature**
  - Use Stripe Java SDK helper or manually:
    - Construct event with `Webhook.constructEvent(payload, signature, webhookSecret)`.
    - Handle exceptions (invalid signature, bad payload).
- **Idempotency**
  - Create a `ProcessedStripeEvent` entity/table:
    - `id` (event id from Stripe).
    - `processedAt`.
  - On each webhook:
    - If event id already exists -> ignore and return `200`.
    - Else -> process and store new record.

---

### 6. Order & Cart Flow

- **On Payment Intent creation (PUT `/stripe/payment`)**
  - Validate cart and stock.
  - Option A: create a provisional order linked to cart and `StripePayment` with status `PENDING`.
  - Option B: defer order creation until `payment_intent.succeeded`; still persist `StripePayment` with `shoppingCartId` metadata.

- **On `payment_intent.succeeded` (webhook)**
  - Look up `StripePayment` by Payment Intent ID.
  - If order not yet created:
    - Create order from cart contents.
  - Update:
    - `StripePayment.status = SUCCEEDED`.
    - `Order` status to paid/confirmed.
    - Decrease stock atomically.
    - Clear/close cart.

- **On `payment_intent.payment_failed` (webhook)**
  - Update `StripePayment.status = FAILED`.
  - Optionally set `Order` status to failed/cancelled (if provisional).

---

### 7. Security & Validation

- **Environment validation**
  - Reuse existing schema validation for environment variables to enforce:
    - `stripe.api-key`
    - `stripe.webhook-secret`
  are present and non-empty.
- **Input validation**
  - Validate `shopping_cart_id` > 0.
  - Ensure authenticated user owns the cart.
- **Error handling**
  - Use global exception filter for:
    - Stripe API errors.
    - Invalid signatures.
    - Order/cart not found.

---

### 8. Testing Checklist

- **Unit tests**
  - `StripePaymentService`:
    - Amount calculation per cart.
    - Handling of empty/invalid carts.
  - `WebhookService`:
    - `payment_intent.succeeded` updates order & payment.
    - `payment_intent.payment_failed` updates status only.
    - Idempotent behavior on repeated events.
- **Integration tests**
  - `PUT /stripe/payment` with a valid cart.
  - `GET /orders/status/{shoppingCartId}` after a successful webhook.
  - `POST /stripe/webhook` with:
    - Valid signature and succeeded event.
    - Valid signature and failed event.
    - Invalid signature (should be rejected).

Use this document as the implementation plan to wire Stripe into the existing e‑commerce API while respecting the OpenAPI spec and the project requirements.

