# 05 — Module Structure

## The tree

```text
src/
├── catalog/                       module — products and suppliers
│   ├── api/                       public API of the module
│   │   ├── catalogFacade.js       public entry point for other modules
│   │   └── contracts/             request and response objects
│   └── internal/                  everything else; unreachable from outside
│       ├── domain/                product and supplier rules
│       ├── application/           catalog use cases
│       └── persistence/           database access for catalog tables
├── inventory/                     module — inventory management
│   ├── api/                       public API of the module
│   │   ├── inventoryFacade.js     public entry point for other modules
│   │   └── contracts/             request and response objects
│   └── internal/                  everything else; unreachable from outside
│       ├── domain/                entries, lots, outputs and FIFO rules
│       ├── application/           inventory use cases
│       └── persistence/           database access for inventory tables
├── users/                         module — application users
│   ├── api/                       public API of the module
│   │   ├── usersFacade.js         public entry point for other modules
│   │   └── contracts/             request and response objects
│   └── internal/                  everything else; unreachable from outside
│       ├── domain/                user rules
│       ├── application/           user use cases
│       └── persistence/           database access for users table
└── platform/                      technical code shared by the application
    ├── config/                    Express and application configuration
    └── errors/                    shared error types and error handler

resources/
├── app-config                     application configuration
└── db/migration/                  versioned changesets for the database

tests/
├── unit/                          mirrors src/, one folder per module
├── module-integration/            tests modules through their public APIs
└── end-to-end/                    tests the complete application
```

## What each folder holds

| Folder | Contains | Never contains |
|---|---|---|
| `<module>/api/` | The public facade and objects that cross the module boundary | Business rules or direct database access |
| `<module>/internal/` | Domain, application and persistence code of the module | Imports from another module's `internal/` |
| `platform/config/` | Express configuration and application setup | Business rules |
| `platform/errors/` | Shared error types and the global error handler | Module-specific business logic |
| `resources/db/migration/` | Ordered and versioned database changes | Manual data fixes |
| `tests/unit/` | Unit tests for domain and application logic | Tests that require the complete application |
| `tests/module-integration/` | Tests that use a module through its public API | Direct access to another module's internals |
| `tests/end-to-end/` | Tests of complete Inven-Track flows | Tests focused only on one internal class |

## Rules that survive any stack

| Rule | Reason |
|---|---|
| One main unit per file, with a filename that identifies the unit | Keeps the project easier to understand and maintain |
| Objects crossing a module boundary should be treated as immutable | Prevents one module from changing another module's data directly |
| `internal/` is never imported outside its own module | This is the rule enforced by the `boundary check` |
| Test folders follow the same module structure as `src/` | Makes missing tests easier to identify |
| One migration folder for the whole application | Inven-Track uses one database and one ordered migration history |
| Each module owns its own database tables | Prevents direct modification of another module's data |
| Modules communicate through their public API | Keeps the module boundaries clear |

## Module responsibilities

| Module | Responsibility | Main data |
|---|---|---|
| `catalog` | Manage products and suppliers | `products`, `suppliers` |
| `inventory` | Manage entries, lots, outputs, FIFO and inventory movements | `entries`, `lots`, `outputs`, `inventory_movements` |
| `users` | Manage application users and responsible users | `users` |
| `platform` | Technical configuration and shared application errors | No business-owned tables |

The `inventory` module is the main module of Inven-Track because it contains
the inventory operations and FIFO logic.

The `catalog` module provides product and supplier information to the
inventory module through its public API.

The `users` module provides user information required to identify the person
responsible for inventory operations.

## Module communication

The main allowed dependencies are:

```text
inventory -> catalog
inventory -> users
```

The `inventory` module may request product, supplier or user information
through the corresponding public API.

The following type of dependency is not allowed:

```text
inventory -> catalog/internal
inventory -> users/internal
```

A module must not access another module's internal implementation directly.

## Inven-Track inventory flow

The main flow through the modules is:

```text
catalog
   |
   | product / supplier information
   v
inventory
   |
   | entry
   v
lot
   |
   | FIFO
   v
output
   |
   v
inventory movement
```

The inventory module uses the product information from `catalog` and the
responsible user information from `users`.

FIFO is handled inside the inventory domain and uses the lot's entry date and
available quantity to determine which lot is consumed first.

## Application configuration

The `platform/config/` folder contains the technical configuration required
to start the Node.js and Express application.

This can include:

- Express configuration.
- Database connection configuration.
- Environment variables.
- Route registration.
- Global middleware.
- Dependency configuration.

Business rules must remain inside the corresponding module and must not be
placed in `platform/config/`.

## If the project grows

If Inven-Track identifies a new bounded context in the future, a new module
can be created following the same structure:

```text
src/
└── new-module/
    ├── api/
    │   ├── newModuleFacade.js
    │   └── contracts/
    └── internal/
        ├── domain/
        ├── application/
        └── persistence/
```

The new module must first be defined in
[`../02-domain/module-boundaries.md`](../02-domain/module-boundaries.md) before
its code is added to the project.

---

**Related:** [`./modular-monolith.md`](./modular-monolith.md) · [`./boundary-enforcement.md`](./boundary-enforcement.md)
