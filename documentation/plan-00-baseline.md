# Plan 00: Baseline and Project Hygiene

## Goal
Establish foundations for safe configuration, error handling, and validation.

## Scope
- Env var validation and config properties
- Global exception handling
- DTO validation and custom annotation

## Inputs
- Requirements: [documentation/requirements.md](documentation/requirements.md)
- Current config: [ecommerce/src/main/resources/application.properties](ecommerce/src/main/resources/application.properties)

## Tasks
- Create a config properties class with validation annotations.
- Add startup validation and fail-fast behavior for missing env vars.
- Add a global exception handler with a standard error payload.
- Add request DTOs with `@Valid`, `@NotNull`, `@Email`.
- Create one custom validation annotation.

## Example Artifacts
- Config: `AppProperties` (jwt.secret, stripe.secret, mail.host)
- Error payload: `{ "code": "VALIDATION_ERROR", "message": "...", "details": [...] }`
- Custom annotation: `@StrongPassword` on signup DTO

## Acceptance Criteria
- Missing env vars cause startup failure with clear message.
- All controllers return consistent error format.
- DTO validation errors are surfaced with field-level details.
