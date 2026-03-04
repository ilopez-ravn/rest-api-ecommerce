# Plan 04: Manager Features (REST + GraphQL)

## Goal
Enable manager-only operations for product and inventory control.

## Scope
- Product CRUD and partial update
- Soft delete and disable
- Product images
- Warehouses and stock updates
- GraphQL manager mutations

## Inputs
- GraphQL schema: [ecommerce/src/main/resources/graphql/Inventory/productSchema.graphqls](ecommerce/src/main/resources/graphql/Inventory/productSchema.graphqls)

## Tasks
- Implement product create, update, patch, delete.
- Log price changes in product_changes_log.
- Implement disable product (visibility off, still stored).
- Implement image upload and delete with validation.
- Implement warehouse CRUD and bulk stock updates.

## Example Artifacts
- POST `/products` (manager only)
- PATCH `/products/{productId}` for partial update
- GraphQL mutation: `disableProduct(productId)`

## Acceptance Criteria
- Manager-only routes blocked for clients.
- Soft delete does not remove DB records.
- Image upload validates type and size.
