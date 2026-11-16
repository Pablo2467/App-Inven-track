# ADR-[NNN] — Inven-Track: [Decision Title]

> This template is used to document architectural and technical decisions for
> **Inven-Track**, a web-based inventory control system for a salsamentaria.
> Decisions may include the modular monolith architecture, FIFO inventory
> management, expiration control, database structure, module boundaries,
> APIs, traceability, and other technical decisions that affect the system.

## Identification

| Field | Value |
|---|---|
| Id | ADR-[NNN] |
| Date | [YYYY-MM-DD — the day the status last changed] |
| Status | [`Proposed` / `Accepted` / `Rejected` / `Superseded by ADR-NNN`] |
| Authors | [names of the people accountable for the decision] |
| Supersedes | [ADR-NNN, or "none"] |

## Context

[What forces the decision now, in three to five sentences. What breaks if it is
not decided. Name the requirements that constrain it — `HU-NN`, `NFR-NN`, or a
`BR-NN` rule from `../../../02-domain/entities-and-rules.md`.]

| Constraint | Source |
|---|---|
| [the constraint, stated as a fact, not a preference] | [`NFR-NN`, a stakeholder, a budget, a deadline] |

For **Inven-Track**, the context may involve decisions related to:

- `catalog` — products and suppliers.
- `inventory` — entries, lots, outputs, FIFO and inventory movements.
- `users` — application users and responsible users.
- `platform` — shared technical configuration and errors.
- FIFO allocation of inventory lots.
- Product expiration and expiration alerts.
- Module boundaries and public APIs.
- Database structure and migration strategy.
- Inventory traceability and movement history.
- REST API design and communication between modules.

## Decision

**We decided:** [one sentence, in the present tense, that a reader can act on.]

[Two or three sentences on the tiebreaker: what made this option win over the
runner-up, not a list of everything good about it.]

The decision must be consistent with the current **Inven-Track** architecture
and with the responsibilities defined for each module.

## Alternatives evaluated

| Alternative | Pros | Cons | Verdict |
|---|---|---|---|
| [Option A — the chosen one] | [what it buys] | [what it costs] | Chosen |
| [Option B] | [what it buys] | [what it costs] | [the one fact that ruled it out] |
| [Option C] | [what it buys] | [what it costs] | [the one fact that ruled it out] |

Alternatives must describe real options that were considered for **Inven-Track**.
Do not add an alternative only to fill the table.

## Consequences

| Kind | Consequence |
|---|---|
| Positive | [something that becomes easier, stated concretely] |
| Positive | [something that becomes cheaper or safer] |
| Cost | [work this decision creates, including who pays it] |
| Risk | [what could go wrong, and the signal that it is going wrong] |

**Documents to update:** [the paths this decision changes, or "none"]

When an ADR changes an existing architectural decision, the affected
documentation must be updated so that the ADR and the current project
documentation do not contradict each other.

## Revisiting

[The condition under which this decision is reopened — a number, a date, or an
observable event. "If it becomes a problem" is not a condition.]

For example, a decision may be revisited when:

- A defined performance threshold is exceeded.
- A new module requires a different dependency structure.
- A business rule changes.
- A new technical requirement makes the current decision insufficient.
- A documented NFR can no longer be satisfied.
- The FIFO or expiration rules of Inven-Track change.
- The inventory process of the salsamentaria changes significantly.

---

**Related:** [`../README.md`](../README.md) · [`../../README.md`](../../README.md)
