Stripe Payment Integration – Testing Plan

 Context

 The Stripe payment integration is fully implemented. This plan covers:
 - Automated tests: Unit tests for services, slice tests for controllers (using Mockito + MockMvc, no DB or external Stripe calls required)
 - Manual testing guide: Step-by-step guide using the Stripe CLI + curl to run the full payment flow end-to-end against a live app

 The project currently has only one placeholder test (EcommerceApplicationTests). Available test infrastructure: JUnit 5, Mockito 5.x (supports MockedStatic),      
 AssertJ, Spring MockMvc, Spring Security Test — all via existing pom.xml dependencies.

 ---
 Part 1 – Automated Tests

 No new pom.xml dependencies needed

 spring-boot-starter-test (already present) ships with Mockito 5.x which supports mockStatic() — required to mock Stripe SDK static calls (PaymentIntent.create(),  
 Webhook.constructEvent()).

 Test resource override

 New file: ecommerce/src/test/resources/application.properties

 Prevents tests from attempting real DB / Stripe connections:
 spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
 spring.datasource.username=postgres
 spring.datasource.password=admin
 spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

 stripe.api-key=sk_test_placeholder
 stripe.webhook-secret=whsec_placeholder
 stripe.currency=usd

 spring.jpa.hibernate.ddl-auto=none

 For @WebMvcTest and @ExtendWith(MockitoExtension.class) tests, no DB connection is made — this file is only needed if @SpringBootTest tests are added later.       

 ---
 Test File 1: StripePaymentServiceTest

 Path: ecommerce/src/test/java/co/ravn/ecommerce/Services/Payments/StripePaymentServiceTest.java

 Approach: @ExtendWith(MockitoExtension.class) — pure unit tests, all dependencies mocked.

 Static mock: PaymentIntent.create() mocked via mockStatic(PaymentIntent.class).

 Test cases:

 Test: createPaymentIntent_happyPath
 Setup: Active cart, user owns it, items in stock, no existing order
 Expected: Returns 201 with client_secret; SaleOrder and StripePayment saved
 ────────────────────────────────────────
 Test: createPaymentIntent_cartNotFound
 Setup: ShoppingCartRepository returns empty
 Expected: Throws ResourceNotFoundException
 ────────────────────────────────────────
 Test: createPaymentIntent_cartNotOwnedByUser
 Setup: Cart client ID ≠ user person ID
 Expected: Returns 403 FORBIDDEN
 ────────────────────────────────────────
 Test: createPaymentIntent_emptyCart
 Setup: Cart has no items
 Expected: Returns 400 BAD_REQUEST
 ────────────────────────────────────────
 Test: createPaymentIntent_insufficientStock
 Setup: Total stock across warehouses < cart quantity
 Expected: Returns 400 BAD_REQUEST
 ────────────────────────────────────────
 Test: createPaymentIntent_idempotent
 Setup: Existing SaleOrder + StripePayment found for cart
 Expected: Returns 201 with same existing client_secret; no new Stripe call
 ────────────────────────────────────────
 Test: getOrderStatus_found
 Setup: SaleOrder and StripePayment exist
 Expected: Returns 200 with OrderStatusResponse
 ────────────────────────────────────────
 Test: getOrderStatus_orderNotFound
 Setup: SaleOrderRepository returns empty
 Expected: Throws ResourceNotFoundException

 Setup pattern:
 @ExtendWith(MockitoExtension.class)
 class StripePaymentServiceTest {

     @Mock StripeConfig stripeConfig;
     @Mock UserRepository userRepository;
     @Mock ShoppingCartRepository shoppingCartRepository;
     @Mock ShoppingCartDetailsRepository shoppingCartDetailsRepository;
     @Mock ProductStockRepository productStockRepository;
     @Mock SaleOrderRepository saleOrderRepository;
     @Mock StripePaymentRepository stripePaymentRepository;

     @InjectMocks StripePaymentService stripePaymentService;

     // SecurityContextHolder stubbed via MockitoAnnotations + static mock of SecurityContext
 }

 Stripe static mock pattern:
 try (MockedStatic<PaymentIntent> piMock = mockStatic(PaymentIntent.class)) {
     PaymentIntent mockIntent = mock(PaymentIntent.class);
     when(mockIntent.getId()).thenReturn("pi_test_123");
     when(mockIntent.getClientSecret()).thenReturn("pi_test_123_secret");
     piMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class))).thenReturn(mockIntent);
     // ... call service and assert
 }

 ---
 Test File 2: WebhookServiceTest

 Path: ecommerce/src/test/java/co/ravn/ecommerce/Services/Payments/WebhookServiceTest.java

 Approach: @ExtendWith(MockitoExtension.class) — all repositories mocked.

 Static mock: Webhook.constructEvent() mocked via mockStatic(Webhook.class).

 Test cases:

 Test: handleEvent_succeeded_updatesOrderAndStock
 Setup: Valid signature, payment_intent.succeeded, matching StripePayment in DB
 Expected: Payment status SUCCEEDED, Order isActive=true, stock reduced, cart PROCESSED, event log saved
 ────────────────────────────────────────
 Test: handleEvent_succeeded_idempotent
 Setup: Same event ID already in processed_stripe_event
 Expected: existsById() returns true → early return; no DB writes
 ────────────────────────────────────────
 Test: handleEvent_failed_updatesStatus
 Setup: Valid signature, payment_intent.payment_failed, matching StripePayment
 Expected: Payment status FAILED, event log saved
 ────────────────────────────────────────
 Test: handleEvent_invalidSignature
 Setup: Webhook.constructEvent() throws SignatureVerificationException
 Expected: Exception propagates to controller
 ────────────────────────────────────────
 Test: handleEvent_unknownPaymentIntent
 Setup: payment_intent.succeeded event but no matching StripePayment in DB
 Expected: Logs error, graceful return (no exception)
 ────────────────────────────────────────
 Test: handleEvent_unknownEventType
 Setup: Event type charge.succeeded
 Expected: No handler called, still marked as processed

 Webhook static mock pattern:
 try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
     Event mockEvent = buildMockSucceededEvent(); // helper to create a mock Event
     webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                .thenReturn(mockEvent);
     // ... call service and assert
 }

 ---
 Test File 3: StripeControllerTest

 Path: ecommerce/src/test/java/co/ravn/ecommerce/Controllers/Payments/StripeControllerTest.java

 Approach: @WebMvcTest(StripeController.class) — loads only the web layer; StripePaymentService and WebhookService are @MockBean.

 Test cases:

 ┌──────────────────────────────────────────┬──────────────────────────────────────────────────────────────────────────┬──────────────────────────────────┐
 │                   Test                   │                                  Setup                                   │             Expected             │
 ├──────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┼──────────────────────────────────┤
 │ putPayment_validRequest_returns201       │ Authenticated CLIENT user; service returns 201 body                      │ HTTP 201, body has client_secret │
 ├──────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┼──────────────────────────────────┤
 │ putPayment_unauthenticated_returns401    │ No JWT header                                                            │ HTTP 401 or 403                  │
 ├──────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┼──────────────────────────────────┤
 │ postWebhook_validSignature_returns200    │ webhookService.handleStripeEvent() runs without exception                │ HTTP 200                         │
 ├──────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┼──────────────────────────────────┤
 │ postWebhook_invalidSignature_returns400  │ webhookService.handleStripeEvent() throws SignatureVerificationException │ HTTP 400 with error message      │
 ├──────────────────────────────────────────┼──────────────────────────────────────────────────────────────────────────┼──────────────────────────────────┤
 │ postWebhook_noSignatureHeader_returns400 │ Missing Stripe-Signature header                                          │ HTTP 400                         │
 └──────────────────────────────────────────┴──────────────────────────────────────────────────────────────────────────┴──────────────────────────────────┘

 Pattern:
 @WebMvcTest(StripeController.class)
 @Import(SecurityConfig.class)
 class StripeControllerTest {

     @Autowired MockMvc mockMvc;
     @MockBean StripePaymentService stripePaymentService;
     @MockBean WebhookService webhookService;
     // Other @MockBeans needed by SecurityConfig (JwtAuthFilter, UserDetailsService, etc.)

     @Test
     void putPayment_validRequest_returns201() throws Exception {
         when(stripePaymentService.createOrRetrievePaymentIntent(anyInt()))
             .thenReturn(ResponseEntity.status(201).body(new PaymentIntentResponse("pi_secret")));

         mockMvc.perform(put("/api/v1/stripe/payment")
                 .header("Authorization", "Bearer " + validJwtToken)
                 .contentType(MediaType.APPLICATION_JSON)
                 .content("{\"shopping_cart_id\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.client_secret").value("pi_secret"));
     }
 }

 ---
 Test File 4: OrderControllerTest

 Path: ecommerce/src/test/java/co/ravn/ecommerce/Controllers/Order/OrderControllerTest.java

 Approach: @WebMvcTest(OrderController.class) + @MockBean StripePaymentService.

 Test cases:

 ┌───────────────────────────────────────────┬───────────────────────────────────────────────┐
 │                   Test                    │                   Expected                    │
 ├───────────────────────────────────────────┼───────────────────────────────────────────────┤
 │ getOrderStatus_found_returns200           │ HTTP 200, body has id, isActive, payment_info │
 ├───────────────────────────────────────────┼───────────────────────────────────────────────┤
 │ getOrderStatus_notFound_returns404        │ ResourceNotFoundException → HTTP 404          │
 ├───────────────────────────────────────────┼───────────────────────────────────────────────┤
 │ getOrderStatus_unauthenticated_returns401 │ No JWT → HTTP 401                             │
 └───────────────────────────────────────────┴───────────────────────────────────────────────┘

 ---
 Part 2 – Manual Testing Guide

 Prerequisites

 - App running: cd ecommerce && mvn spring-boot:run
 - processed_stripe_event table applied to DB (from migrations/schema.sql)
 - https://stripe.com/docs/stripe-cli installed (stripe login)
 - Real Stripe test API key set in application.properties (stripe.api-key=sk_test_...)
 - A seed user with CLIENT role and a seed product/warehouse in the DB

 ---
 Step 1 – Authenticate as CLIENT

 curl -s -X POST http://localhost:8080/api/v1/users/login \
   -H "Content-Type: application/json" \
   -d '{"username": "client_user", "password": "password"}' | jq .

 Save the accessToken value as $TOKEN.

 ---
 Step 2 – Create / Confirm Active Cart with Items

 If no cart exists for the client, create one:
 curl -s -X POST http://localhost:8080/api/v1/carts \
   -H "Authorization: Bearer $TOKEN" \
   -H "Content-Type: application/json" \
   -d '{"products": [{"product_id": 1, "price": 25.00, "quantity": 2}]}' | jq .

 Save the cart id as $CART_ID.

 ---
 Step 3 – Create a Payment Intent

 curl -s -X PUT http://localhost:8080/api/v1/stripe/payment \
   -H "Authorization: Bearer $TOKEN" \
   -H "Content-Type: application/json" \
   -d "{\"shopping_cart_id\": $CART_ID}" | jq .

 Expected: HTTP 201, body { "client_secret": "pi_xxx_secret_xxx" }.

 Call the same endpoint again with the same $CART_ID — should return same client_secret (idempotent, 200/201 no new PaymentIntent created).

 ---
 Step 4 – Forward Webhooks via Stripe CLI

 In a separate terminal, start webhook forwarding:
 stripe listen --forward-to http://localhost:8080/api/v1/stripe/webhook

 The CLI prints a webhook signing secret (e.g., whsec_...). Update application.properties:
 stripe.webhook-secret=whsec_...  # from CLI output
 Restart the app.

 ---
 Step 5 – Trigger a Succeeded Event

 stripe trigger payment_intent.succeeded

 Expected in app logs: "Stripe payment succeeded" processing; DB should show:
 - stripe_payment.payment_status = 'SUCCEEDED'
 - sale_order.is_active = true
 - shopping_cart.status = 'PROCESSED'
 - product_stock.quantity reduced
 - New row in stripe_payment_event_log
 - New row in processed_stripe_event

 Trigger the same event again (same id):
 stripe trigger payment_intent.succeeded
 Expected: App logs "Duplicate Stripe event ignored" — no DB changes.

 ---
 Step 6 – Test Invalid Signature

 curl -s -X POST http://localhost:8080/api/v1/stripe/webhook \
   -H "Content-Type: application/json" \
   -H "Stripe-Signature: t=fake,v1=invalidsignature" \
   -d '{"type": "payment_intent.succeeded"}' | jq .

 Expected: HTTP 400, body "Invalid Stripe signature".

 ---
 Step 7 – Trigger a Failed Payment Event

 stripe trigger payment_intent.payment_failed

 Expected in DB:
 - stripe_payment.payment_status = 'FAILED'
 - sale_order.is_active = false (unchanged)
 - New stripe_payment_event_log row with status FAILED

 ---
 Step 8 – Check Order Status

 curl -s http://localhost:8080/api/v1/orders/status/$CART_ID \
   -H "Authorization: Bearer $TOKEN" | jq .

 Expected: HTTP 200, body with id, isActive, orderDate, and payment_info.status = "SUCCEEDED".

 Test with unknown ID:
 curl -s http://localhost:8080/api/v1/orders/status/99999 \
   -H "Authorization: Bearer $TOKEN" | jq .

 Expected: HTTP 404.

 ---
 Files to Create

 ┌────────┬───────────────────────────────────────────────────────────────────────────────────────────┐
 │ Action │                                           Path                                            │
 ├────────┼───────────────────────────────────────────────────────────────────────────────────────────┤
 │ CREATE │ ecommerce/src/test/resources/application.properties                                       │
 ├────────┼───────────────────────────────────────────────────────────────────────────────────────────┤
 │ CREATE │ ecommerce/src/test/java/co/ravn/ecommerce/Services/Payments/StripePaymentServiceTest.java │
 ├────────┼───────────────────────────────────────────────────────────────────────────────────────────┤
 │ CREATE │ ecommerce/src/test/java/co/ravn/ecommerce/Services/Payments/WebhookServiceTest.java       │
 ├────────┼───────────────────────────────────────────────────────────────────────────────────────────┤
 │ CREATE │ ecommerce/src/test/java/co/ravn/ecommerce/Controllers/Payments/StripeControllerTest.java  │
 ├────────┼───────────────────────────────────────────────────────────────────────────────────────────┤
 │ CREATE │ ecommerce/src/test/java/co/ravn/ecommerce/Controllers/Order/OrderControllerTest.java      │
 └────────┴───────────────────────────────────────────────────────────────────────────────────────────┘
