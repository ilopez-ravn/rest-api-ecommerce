# Plan 03: Catalog and Public Access (REST)

## Goal
Expose product catalog endpoints with pagination and filters.

## Scope
- Public product list and detail
- Categories and tags
- Filtering, sorting, pagination

## Inputs
- OpenAPI: [documentation/open-api.spec.yml](documentation/open-api.spec.yml)

## Tasks
[x] Implement GET `/products` with pagination and filters.
[x] Implement GET `/products/{productId}` with images and stock info.
[x] Implement GET `/categories` and GET `/tags`.
[ ] Enforce visibility rules: active and not deleted only.

## Example Artifacts
- Query params: `page`, `size`, `sort`, `categoryId`
- Response page info: `current_page`, `total_pages`, `total_count`

## Acceptance Criteria
- Product list is public and paginated.
- Product detail shows images and stock status.
- Empty category returns empty list with 200.
