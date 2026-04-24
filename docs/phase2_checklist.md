# Phase 2 Checklist

This checklist tracks the remaining security, authorization, and domain-hardening work for Phase 2.

## Ownership and Authorization

- [x] Harden review ownership and permissions
- [x] Restrict company management to the correct owner or admin
- [x] Restrict ground management to the correct owner or admin
- [x] Restrict time slot management to the correct owner or admin
- [ ] Restrict report generation and access to the correct owner or admin
- [ ] Restrict file upload/delete actions to the correct owner or admin

## Domain Integrity

- [ ] Tighten booking status transition rules
- [ ] Tighten payment status transition rules
- [x] Remove or restrict unsafe direct slot mutation flows

## Test Coverage

- [x] Add review authorization tests
- [x] Add company/ground/slot authorization tests
- [ ] Add report authorization tests
- [ ] Add file authorization tests
- [ ] Add state-transition tests for booking/payment flows

## Notes

- Phase 2 is complete when ownership is consistently derived from authentication or enforced server-side, and the remaining sensitive flows are covered by tests.
