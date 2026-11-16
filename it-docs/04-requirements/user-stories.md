# 04 — User Stories

## Story identifier

`HU-NN` — sequential, two digits, starting at `01`. An id is never reused,
even for a story that gets dropped before it ships.

## Backlog

| HU-NN | Title | Module | Priority |
|---|---|---|---|
| HU-01 | Register a new product | `catalog` | ⭐ |
| HU-02 | Register a supplier | `catalog` | ⭐ |
| HU-03 | Register an inventory entry and lot | `inventory` | ⭐ |
| HU-04 | Register an inventory output | `inventory` | ⭐ |
| HU-05 | Apply FIFO when registering an output | `inventory` | ⭐ |
| HU-06 | View products near expiration | `inventory` | ⭐ |
| HU-07 | View inventory movements and traceability | `inventory` | — |
| HU-08 | Manage application users | `users` | — |

## Writing a new story

Copy [`_template-hu.md`](./_template-hu.md), assign the next unused `HU-NN`,
fill in the narrative and Gherkin acceptance criteria, then add a row above.
A story whose acceptance criteria cannot be checked by reading the running
application is not ready for the backlog.

---

**Related:** [`./_template-hu.md`](./_template-hu.md) · [`./traceability-matrix.md`](./traceability-matrix.md)
