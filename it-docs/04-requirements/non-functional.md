# 04 — Non-Functional Requirements

## What makes a requirement here valid

| Not this | This |
|---|---|
| The system must be fast | p95 under 300 ms at 50 concurrent users |
| The system must be secure | Zero write endpoints accept an unauthenticated request |

## Requirements

| NFR-NN | Category | Threshold | How it is measured |
|---|---|---|---|
| NFR-01 | Performance | p95 response time under 300 ms at 50 concurrent users, on product, lot and inventory read endpoints | Scripted load test against staging before each release |
| NFR-02 | Performance | FIFO allocation for an output involving up to 20 active lots completes in under 2 seconds | Timed automated test against the staging dataset before release |
| NFR-03 | Availability | 99.5% uptime per month, measured across the published business-hours support window | Uptime monitor polling `/health` every 5 minutes |
| NFR-04 | Availability | Database restore from the latest backup completes in under 30 minutes | Quarterly restore drill, timed and logged |
| NFR-05 | Security | 100% of write endpoints for products, lots, entries and outputs reject an unauthenticated request | Automated authorization test suite, run in CI on every pull request |
| NFR-06 | Security | Zero critical- or high-severity dependency findings merged without a written, expiring waiver | CI dependency-scan gate, see [`security-policy.md`](../00-governance/security-policy.md) |
| NFR-07 | Maintainability | Unit test coverage at least 80% on changed files and at least 80% on inventory and FIFO logic | CI coverage gate, see [`definition-of-done.md`](../00-governance/definition-of-done.md) |
| NFR-08 | Maintainability | A developer runs the application locally in under 1 hour, start to first successful request | Timed onboarding checklist signed off by the tech lead |
| NFR-09 | Observability | 100% of unhandled errors are logged with a request id and a timestamp | Manual log sample reviewed at every release |
| NFR-10 | Observability | A support question about one inventory movement can be traced through the logs in under 5 minutes | Quarterly support drill, timed |

## Identifier format

`NFR-NN` — same two-digit, sequential, never-reused convention as `HU-NN`.

---

**Related:** [`../00-governance/documentation-rules.md`](../00-governance/documentation-rules.md) · [`./traceability-matrix.md`](./traceability-matrix.md)
