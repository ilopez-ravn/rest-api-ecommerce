# Plan 05: Client Features (REST + GraphQL)

## Goal
Enable client shopping workflows: carts, likes, and client data.

## Scope
- Client CRUD
- Cart lifecycle
- Like/unlike products
- Client GraphQL mutations

## Inputs
- GraphQL schema: [ecommerce/src/main/resources/graphql/Sales/cartSchema.graphqls](ecommerce/src/main/resources/graphql/Sales/cartSchema.graphqls)

## Tasks
- Implement client CRUD endpoints.
- Implement cart create, update, delete, add items, update quantity.
- Validate stock before add and checkout.
- Implement like/unlike product actions.

## Example Artifacts
- POST `/carts/{cartId}/items` (add product)
- PATCH `/carts/{cartId}/items/{itemId}` (update quantity)
- GraphQL mutation: `likeProduct(productId)`

## Acceptance Criteria
- Cart updates respect stock limits.
- Likes are restricted to authenticated users.
- Client endpoints require auth where applicable.
