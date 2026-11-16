# 05 — Layered Architecture

## The four layers

| Layer | Responsibility | May depend on |
|---|---|---|
| Presentation | Receive HTTP requests, validate input, call the application logic and return responses | Application, transport objects |
| Application | Coordinate use cases such as registering entries, creating lots and processing outputs | Persistence, Domain |
| Persistence | Store and retrieve products, lots, entries, outputs and users | Domain |
| Domain | Define entities, inventory rules, FIFO logic and invariants | Nothing |

Concrete folder names for the Node.js application are defined in
[`module-structure.md`](./module-structure.md). The responsibilities above
remain the same regardless of the framework used.

## Dependency direction

```text
Presentation  ->  Application  ->  Persistence  ->  Domain
```

| Rule | Why it holds |
|---|---|
| Dependencies point one way only, left to right | Prevents cycles between layers and makes each layer easier to test |
| Domain depends on nothing outside itself | FIFO and inventory rules must be testable without a database or web framework |
| Presentation never calls Persistence directly | Database access must be handled through the application layer |
| Application never contains HTTP-specific logic | Business rules should not depend directly on Express |
| Persistence does not contain business decisions | Its responsibility is to read and write data |
| Framework configuration stays separate from business logic | Keeps application rules easier to maintain and review |

## Inven-Track responsibilities

The four layers are applied to the main inventory operations:

| Operation | Main responsibility |
|---|---|
| Register product | Presentation receives the request and Application coordinates the creation |
| Register inventory entry | Application validates the operation and Persistence stores the information |
| Create lot | Application creates the lot with its entry and expiration information |
| Register output | Application applies the inventory rules and determines the lot according to FIFO |
| Check expiration | Application evaluates lot expiration information |
| Record inventory movement | Application coordinates the operation and Persistence stores the history |

The domain layer contains the rules that should remain independent from
Express, the database and other infrastructure.

In particular, FIFO must not depend on an HTTP request or a database query
directly. The application layer provides the required inventory information
and the domain logic determines how the rule is applied.

## When flat layers stop being enough

Inven-Track currently has one bounded context focused on inventory management.

The deciding signal for introducing additional modules is the number of
bounded contexts identified in
[`../02-domain/domain-map.md`](../02-domain/domain-map.md).

| Signal | Flat layers still fit | Time to move to modules |
|---|---|---|
| Bounded contexts on the domain map | exactly 1 | 2 or more |
| Persistent entities | up to ~10 | more than ~10 |
| Developers committing in a normal week | up to 3 | 4 or more |
| Cross-area collisions | rare | two people edit the same application-layer file weekly |

The first row is the main trigger. The other rows are supporting signals.

If Inven-Track remains within one bounded context, the four-layer structure
should be maintained instead of creating additional technical modules without
a clear domain reason.

## How the move happens

If the project grows and a second bounded context is identified:

| Step | What changes |
|---|---|
| 1 | Draw the second bounded context in [`../02-domain/domain-map.md`](../02-domain/domain-map.md) and name its module |
| 2 | Move that context's classes out of the shared layer folders into its own module folder |
| 3 | Give the new module a public API and keep its internal implementation private |
| 4 | Enable the `boundary check` described in [`boundary-enforcement.md`](./boundary-enforcement.md) |
| 5 | Record the architectural change in a new ADR |

Layers do not disappear when the project moves to modules. Each module keeps
the same responsibilities internally.

The resulting structure can therefore evolve into a modular monolith without
changing the basic responsibilities of Presentation, Application,
Persistence and Domain.

---

**Related:** [`./modular-monolith.md`](./modular-monolith.md) · [`./module-structure.md`](./module-structure.md)
