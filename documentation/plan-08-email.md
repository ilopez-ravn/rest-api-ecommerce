# Plan 08: Email Notifications (Async)

## Goal
Send async email notifications for key events.

## Scope
- Password change
- Order status update
- Low stock alerts
- Discount alerts

## Inputs
- Requirements: [documentation/requirements.md](documentation/requirements.md)

## Tasks
[x] Implement async email sender service.
[ ] Create templates for each email type.
[-] Persist email logs and failure reasons.
[x] Trigger emails from domain events.

## Example Artifacts
- Template: `password-change.html`
- Email log entry: userId, subject, status
- Trigger: order status update -> send email

## Acceptance Criteria
- Email sending does not block API responses.
- Failures are logged without crashing.
- Templates are reused and versioned.
