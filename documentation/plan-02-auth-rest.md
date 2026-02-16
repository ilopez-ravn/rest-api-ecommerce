# Plan 02: Authentication (REST)

## Goal
Implement complete auth flow with JWT and refresh tokens.

## Scope
- Sign up, sign in, sign out
- Refresh token flow
- Forgot and reset password

## Inputs
- OpenAPI: [documentation/open-api.spec.yml](documentation/open-api.spec.yml)

## Tasks
[x] Implement signup, login, logout endpoints.
[x] Add refresh token persistence and rotation.
[x] Add password reset token generation and expiry.
[x] Add email notification on password change.

## Example Artifacts
- POST `/users/login` -> returns JWT + refresh token
- POST `/users/refresh` -> returns new JWT
- POST `/users/password/token` -> creates reset token
- PUT `/users/password` -> updates password

## Acceptance Criteria
- Passwords are hashed with BCrypt.
- Reset token expires and cannot be reused.
- Login and refresh return valid JWTs.
