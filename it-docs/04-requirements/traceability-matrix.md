# 04 — Traceability Matrix

## How to read these tables

`Requirement -> Module -> Design document -> Test case`. Both tables read that
way: the first closes every `HU-NN`, the second every `NFR-NN`. A cell may point
at a section filled in later than this one — that forward link is expected, not
a defect.

## Stories

| HU-NN | Module | Design document | Test case |
|---|---|---|---|
| HU-01 | `catalog` | [`data-models.md`](../06-data/data-models.md) — products | [`unit`](../11-quality/testing-strategy.md) — reject a product with an invalid price or stock |
| HU-02 | `catalog` | [`data-models.md`](../06-data/data-models.md) — suppliers | [`unit`](../11-quality/testing-strategy.md) — reject incomplete supplier information |
| HU-03 | `inventory` | [`data-models.md`](../06-data/data-models.md) — entries and lots | [`module-integration`](../11-quality/testing-strategy.md) — registering an entry creates the corresponding lot |
| HU-04 | `inventory` | [`data-models.md`](../06-data/data-models.md) — lots and inventory movements | [`module-integration`](../11-quality/testing-strategy.md) — an output reduces the available quantity of the correct lot |
| HU-05 | `inventory` | [`entities-and-rules.md`](../02-domain/entities-and-rules.md) — FIFO rule | [`unit`](../11-quality/testing-strategy.md) — output consumes the oldest available lot first |
| HU-06 | `inventory` | [`entities-and-rules.md`](../02-domain/entities-and-rules.md) — expiration rule | [`module-integration`](../11-quality/testing-strategy.md) — products near expiration are included in the alert |
| HU-07 | `inventory` | [`data-models.md`](../06-data/data-models.md) — inventory movements | [`end-to-end`](../11-quality/testing-strategy.md) — an entry and output can be traced through their inventory movements |
| HU-08 | `users` | [`data-models.md`](../06-data/data-models.md) — users | [`unit`](../11-quality/testing-strategy.md) — inactive users cannot perform inventory operations |

Every Design document cell points to the document that defines the data or
business rules used by the corresponding story. When a dedicated module
document is created under `09-modules/`, the Design document column should be
updated to point to that module's document.

The Test case column currently identifies the type of verification. Once the
individual test cases are created, each cell should point to the corresponding
`TC-NNN` from [`_template-test-case.md`](../11-quality/_template-test-case.md).

## Non-functional requirements

| NFR-NN | Module | Design document | Test case |
|---|---|---|---|
| NFR-01 | whole artifact | [`observability.md`](../13-operations/observability.md) — request duration and API performance | Scripted load test against staging, before each release |
| NFR-02 | `inventory` | [`entities-and-rules.md`](../02-domain/entities-and-rules.md) — FIFO allocation rule | Timed automated test with up to 20 active lots |
| NFR-03 | whole artifact | [`observability.md`](../13-operations/observability.md) — `GET /health` | Uptime monitor polling `/health` every 5 minutes |
| NFR-04 | whole artifact | none yet — see Gaps | Quarterly database restore drill, timed and logged |
| NFR-05 | `catalog`, `inventory`, `users` | [`rest-conventions.md`](../07-api/rest-conventions.md) — authentication and authorization rules | Automated authorization suite in CI on every pull request |
| NFR-06 | whole artifact | [`security-policy.md`](../00-governance/security-policy.md) — dependency scanning | CI dependency-scan gate |
| NFR-07 | whole artifact | [`definition-of-done.md`](../00-governance/definition-of-done.md) — coverage threshold | CI coverage gate on every pull request |
| NFR-08 | whole artifact | [`local-setup.md`](../10-devops/local-setup.md) — prerequisites and startup steps | Timed onboarding run, signed off by the tech lead |
| NFR-09 | whole artifact | [`observability.md`](../13-operations/observability.md) — structured error log fields | Log sample reviewed at every release |
| NFR-10 | `inventory` | [`observability.md`](../13-operations/observability.md) — request ID and inventory movement traceability | Quarterly support drill, timed |

Every row above is proved by a gate, a monitor, a timed run or a scheduled
drill rather than only by a story scenario. These checks complement the three
testing layers described in
[`testing-strategy.md`](../11-quality/testing-strategy.md).

Each requirement should eventually have a `TC-NNN` copied from
[`_template-test-case.md`](../11-quality/_template-test-case.md), with the
corresponding verification written into its Steps.

## Gaps

A gap is an `HU-NN` or `NFR-NN` with no row above, or a row whose design
document or test case does not exist yet.

Current gaps:

- **NFR-04** sets a 30-minute database restore target, but the repository does
  not yet contain a document describing the restore procedure.
- The `HU-NN` rows identify the expected traceability for the initial Inven-Track
  stories, but the individual HU documents still need to be created.
- The `TC-NNN` test cases still need to be created and linked to each row.
- Dedicated module documents under `09-modules/` may replace the general design
  documents as those modules are documented.

Once a real test case exists for a row, its Test case column should carry that
test case's `Id` from
[`_template-test-case.md`](../11-quality/_template-test-case.md), in place of
the test type or general check named above.

---

**Related:** [`./user-stories.md`](./user-stories.md) · [`./non-functional.md`](./non-functional.md)
