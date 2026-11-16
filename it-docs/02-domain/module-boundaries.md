# 02 — Module Boundaries

## What a module is

A **module** is a bounded context that lives inside the single deployable application. It owns its data, exposes a narrow **public API**, and keeps its implementation **internal**.

| Term | Meaning | Enforced by |
|---|---|---|
| Module | A bounded context with its own folder and its own tables | Folder layout |
| Public API | The only entry point other parts of the application may call | Boundary rules |
| Internal | Everything else inside the module | Boundary rules |

## How to draw the boundary

1. Group behavior by the **language the business uses**, not by technical layer.

   `Inventory` is a module; `Controllers`, `Services`, and `Repositories` are implementation layers inside that module.

2. A module should own the data it writes. If two modules write the same table, the ownership is unclear.

3. Prefer **fewer, larger** modules at the start. Iven-Track currently has one main bounded context.

## Module map

| Module | Owns | Public API | Depends on |
|---|---|---|---|
| `inventory` | products, stock entries, stock exits, inventory rules | `InventoryFacade` | — |

## Signals the boundary is wrong

| Signal | What it means |
|---|---|
| Product and stock behavior are split across several modules | The boundary may be separating one business workflow |
| Two modules write the same inventory table | Ownership is not settled |
| FIFO logic is implemented outside the inventory module | The business rule is in the wrong place |
| A module contains unrelated business concepts | The boundary may be too broad |

---

**Related:** [`../05-architecture/modular-monolith.md`](../05-architecture/modular-monolith.md) · [`../09-modules/module-inventory.md`](../09-modules/module-inventory.md)