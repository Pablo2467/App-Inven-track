# 00 — Definition of Done

A story is done when every applicable criterion below is satisfied. Code
compiling successfully is not sufficient by itself.

## Checklist

| # | Criterion | How it is verified |
|---|---|---|
| 1 | All acceptance criteria are implemented | Reviewer verifies the criteria against the running application |
| 2 | Tests for changed business behavior pass | Automated tests are executed successfully |
| 3 | FIFO behavior is preserved when inventory logic changes | FIFO-related tests pass when affected |
| 4 | No unintended module dependency is introduced | Reviewer checks the affected module dependencies |
| 5 | Database changes are documented and reproducible | Required migration or schema change is committed |
| 6 | Error handling follows the project's defined conventions | Reviewer checks the affected error responses and logs |
| 7 | Documentation affected by the change is updated | Documentation is reviewed in the same pull request |
| 8 | Pull request review requirements are satisfied | Required approval and configured checks have passed |

## Test expectations

Business rules must have automated tests whenever they can be verified
automatically.

Changes involving stock calculations or FIFO must include tests that verify
the expected inventory behavior.

## Merge condition

A story is considered Done only when the implementation, tests,
documentation, and pull request review requirements have all been completed.

---

**Related:** [`definition-of-ready.md`](./definition-of-ready.md) · [`README.md`](./README.md)