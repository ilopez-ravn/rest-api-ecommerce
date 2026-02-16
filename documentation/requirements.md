# Homework: Build Your API Store

Build a complete e-commerce API for a store of your choice.

## Technical Requirements

### Core Technologies

* **PostgreSQL:** Database
* **ORM:** Hibernate (or any of your preference)
* **Spring Boot:** Application framework

### Mandatory Implementations

All projects must include:

* **Schema validation for environment variables:** Validate configuration at startup
* **Global exception filter:** Centralized error handling across the application
* **Bean validations and annotations:** Use `@Valid`, `@NotNull`, `@Email`, etc.
* **Custom annotations:** Create at least one custom validation or behavior annotation
* **Security configurations:**
  * HTTP exploit prevention (CSRF, XSS, etc.)
  * CORS configuration
  * Rate limiting (required specifically for password reset endpoint)


---

## Mandatory Features

### 1. Authentication System (REST)

Build complete user authentication with the following endpoints:

**Required Endpoints:**

* Sign up
* Sign in
* Sign out
* Forgot password
* Reset password

**Requirements:**

* Secure password storage (hashing)
* JWT or session-based authentication
* Password reset tokens with expiration
* Rate limiting on reset password (prevent abuse)

**Example:** A user should be able to register with email/password, receive a reset token via email if they forget their password, and securely update it.

### 2. Product Catalog (REST)

#### List Products with Pagination

* Return paginated results (e.g., 20 products per page)
* Include total count and page metadata
* Support sorting options (price, name, date added)

#### Search Products by Category

* Filter products by category ID or name
* Combine with pagination
* Return empty results gracefully if category has no products

### 3. User Roles

Implement two distinct user types:

#### Manager Role

* Full product management access
* Order oversight capabilities
* Delivery status updates

#### Client Role

* Product browsing and purchasing
* Order tracking
* Cart and wishlist (liked products) management 

**Requirements:**

* Role-based authorization on all protected endpoints
* Different permissions enforced at service/controller level
* Proper HTTP status codes (403 Forbidden) for unauthorized access

### 4. Email Notification System

Automated emails for the following events:

#### Password Change Notification

**Trigger:** User successfully changes password
**Content:** Confirmation email with timestamp and security notice
**Example:** "Your password was changed on \[date\]. If this wasn't you, contact support immediately."

#### Order Status Updates

**Trigger:** Order status changes (confirmed, shipped, delivered, cancelled)
**Content:** Current status, tracking information, estimated delivery
**Example:** "Your order #12345 has been shipped! Track it here: \[link\]"

#### Low Stock Alerts (Liked Products)

**Trigger:** Liked/favorited product drops below 3 units
**Content:** Product name, current stock level, purchase link
**Example:** "Hurry! Your favorite Chocolate Chip Cookies have only 2 left in stock!"

#### Discount Alerts (Liked Products)

**Trigger:** Liked/favorited product gets a discount
**Content:** Product name, original price, discounted price, savings percentage
**Example:** "Great news! Your liked item 'Organic Dog Food' is now 25% off! Was $40, now $30!"

**Requirements:**

* Use async processing for emails (don't block API responses)
* Template-based emails with professional formatting
* Handle email failures gracefully (log but don't crash)

### 5. Manager Capabilities (REST & GraphQL)

Managers must be able to:

#### Create Products

* Validate all required fields (name, price, category, stock)
* Price must be positive decimal
* Return created product with generated ID

#### Update Products

* Partial updates allowed (PATCH) or full replacement (PUT)
* Validate stock cannot go negative
* Price changes should be logged

#### Delete Products

* Soft delete (mark as deleted) vs hard delete
* Prevent deletion if product has pending orders
* Return success confirmation

#### Disable Products 

* Make product invisible to clients but keep in database
* Don't allow purchases of disabled products
* Managers can still view disabled products

#### View Client Orders with Pagination

* List all orders across all clients
* Support filtering (by status, date range, client)
* Include order items, client info, totals

#### Upload Product Images

* Support multiple images per product (at least 3)
* Validate file types (JPEG, PNG only)
* Resize/compress images for performance
* Store with unique filenames to prevent conflicts

#### Update Delivery Status 

* Valid status transitions only (e.g., can't go from DELIVERED back to PENDING)
* Trigger email notification on status change
* Record timestamp of each status change

### 6. Client Capabilities (REST & GraphQL)

Clients must be able to:

#### View Products

* See all active (non-disabled) products
* No authentication required
* Same as "List Products" endpoint

#### View Product Details (REST)

* Individual product page with full information
* Include images, description, price, stock availability, category
* Show "Out of Stock" if stock is equal to 0

#### Buy Products

* Create order from cart contents
* Reduce stock quantities atomically (handle concurrency)
* Calculate total including any discounts
* Clear cart after successful purchase

#### Add Products to Cart

* Guest carts (session-based) or authenticated user carts
* Update quantity if product already in cart
* Validate stock availability before adding

#### Like Products

* Toggle like/unlike functionality
* Only authenticated users can like products
* Track for notification triggers (low stock, discounts)

#### View My Orders with Pagination

* Show only authenticated user's orders
* Most recent orders first
* Include order status and delivery tracking

#### Track Order Status and Delivery

* Real-time status display (pending, confirmed, shipped, delivered)
* Estimated delivery date if available
* Tracking number for shipped orders

### 7. Public Product Visibility

Product information (including images) must be accessible to both authenticated and non-authenticated users.

**Implementation:**

* Product list and detail endpoints should NOT require authentication
* Only purchasing, cart, and likes require authentication
* Consider caching for better performance

### 8. Stripe Payment Integration

Implement secure payment processing with Stripe.

**Requirements:**

* Create payment intent on checkout
* Handle successful payment webhook
* Handle failed payment webhook
* Secure webhook signature verification (validate Stripe signature)
* **Never store** full card details (let Stripe handle PCI compliance)
* Update order status based on payment status
* Idempotent webhook handling (same event processed once)

## API Protocol Requirements

### Use REST for

* All authentication endpoints
* Product management CRUD operations (Create, Read, Update from REST side)
* Product listing and search

### Use GraphQL for

* Order management (queries and mutations)
* Cart management (mutations)
* Manager features: Delete products, Disable products, Update delivery status
* Client features: Like products (mutation)

## Optional Features (Extra Points)

*Tip: List is in increasing order of difficulty, but feel free to implement the modules you like*  

### 1. GraphQL Resolve Fields

**What:** Implement custom field resolvers for nested data
**Example:** When querying an Order, resolve the `client` field by fetching user data separately rather than eager loading
**Benefit:** Reduces over-fetching and improves performance

### 2. Deployment

**Options:**

* Heroku, Railway, Render (free tiers available)
* AWS, Google Cloud, Azure
* Docker container deployment

**Requirements:**

* Include deployment instructions in README
* Environment variables properly configured

### 3. Refund & Return System

**Requirements:**

* Clients can request refund with reason
* Manager approves/denies refund requests
* Track return shipping status
* Automated emails at each step (refund requested, approved, product received, refund processed)
* Update inventory when returned product received

**Example Flow:**


1. Client requests refund → Email sent to client confirming request
2. Manager reviews → Email sent with approval/denial
3. If approved, client ships back → Tracking updates via email
4. Product received → Stock increased, refund processed, confirmation email

### 4. Auction System with Concurrency Handling

**Requirements:**

* Products can be marked as "auction" items
* Multiple users can place bids simultaneously
* Handle race conditions (optimistic locking or pessimistic locking)
* Auction end time enforcement
* **Bonus:** Real-time bid updates via GraphQL subscriptions

### 5. Recommendation System

**Requirements:**

* Track user behavior (liked products, viewed products)
* Algorithm suggestions:
  * "Users who liked X also liked Y"
  * Recommend products from same categories as liked items
  * Show recently viewed products
* Display recommendations on product pages or homepage

**Example:** If user likes "Organic Dog Food" and "Dog Toys", recommend other pet products or items frequently bought together.

## Resources Summary

### Core Spring Resources

* [Spring Framework Documentation](https://docs.spring.io/spring-framework/reference/overview.html)
* [Spring Quickstart](https://spring.io/quickstart)
* [Spring Academy](https://spring.academy/courses)
* [Spring Initializr](https://start.spring.io/)

### Key Learning Areas

* Inversion of Control (IoC) and Dependency Injection (DI)
* Bean lifecycle and scopes
* Spring Security and OAuth 2.0
* Spring Data JPA and Hibernate
* REST API development
* GraphQL with Spring
* Security best practices

### Key Concepts to Master

* Spring Boot application structure
* Dependency injection and IoC containers
* Bean configuration and annotations
* RESTful web services
* Spring Security (authentication & authorization)
* OAuth 2.0 implementation
* JPA/Hibernate ORM
* Transaction management
* Exception handling
* Validation
* GraphQL integration
* Task scheduling
* Event-driven architecture

## Submission Guidelines

* GitHub repository link
* README with setup instructions
* API documentation (Swagger collection)
* Demo video or deployed link