# 06 — Database Conventions

## One database, divided by module

Inven-Track will use a single database divided logically by module. The
database will use a schema-per-module approach when the selected database
engine supports independent schemas.

| Mechanism | How it works | Enforced by | Reviewer check |
|---|---|---|---|
| **Schema per module** (recommended) | Each module gets its own namespace, such as `catalog.products` and `inventory.lots` | Database permissions and module access rules | Verify that a module does not have direct access to tables outside its own schema |
| Table-prefix per module (fallback) | Tables use a module prefix, such as `catalog_products` and `inventory_lots` | Naming convention and automated validation | Verify that every table uses a recognized module prefix |

The project should default to schema per module when the selected database
engine supports it. Table-prefix naming can be used when the database engine
does not provide independent schemas.

The purpose of this separation is to maintain clear ownership of data inside
the application. Each module is responsible for the tables that belong to
its own business domain.

## Module ownership

For Inven-Track, the initial database organization is:

| Module | Main responsibility | Example tables |
|---|---|---|
| `catalog` | Product and supplier information | `products`, `suppliers` |
| `inventory` | Stock, lots and inventory movements | `lots`, `entries`, `outputs`, `inventory_movements` |
| `users` | Application users and responsible users | `users` |

The final module structure must remain consistent with
[`../02-domain/module-boundaries.md`](../02-domain/module-boundaries.md).

## Module boundaries and foreign keys

No foreign key should cross a module boundary.

For example, if an inventory table needs to identify a product, the
`product_id` value can be stored in the inventory table without creating a
foreign key to a table owned by the `catalog` module.

The relationship is validated through the owning module's public API or
application service.

For example:

`inventory.lots.product_id`

identifies a product owned by the `catalog` module, but the `inventory`
module must not directly modify the product record.

This rule keeps ownership clear and prevents one module from creating
database-level dependencies on another module's tables.

## Naming

| Element | Rule | Example |
|---|---|---|
| Entity — the business concept, not the table | Singular, using the business terminology | `Product`, `Lot`, `Inventory movement` |
| Table | snake_case, plural | `products`, `lots`, `inventory_movements` |
| Column | snake_case | `unit_price`, `entry_date`, `expiration_date` |
| Primary key | `id`, UUID | `id UUID PRIMARY KEY` |
| Foreign key | `<singular table>_id` | `product_id`, `supplier_id`, `lot_id` |
| Boolean | Prefixed with `is_` / `has_` | `is_active`, `is_expired` |
| Index | `idx_<table>_<column>` | `idx_lots_product_id` |

## Inven-Track domain naming

The database must use the same terminology defined in the project glossary.

The main concepts are:

- `Product` — product sold or managed by the salsamentaria.
- `Lot` — quantity of a product received together and identified by a lot
  code.
- `Supplier` — company or person that provides products.
- `Entry` — registration of products entering the inventory.
- `Output` — registration of products leaving the inventory.
- `Inventory movement` — record of an entry, output or other inventory
  adjustment.
- `User` — person responsible for performing operations in the system.

The term `lot` should be used consistently for the inventory batch concept.
The application should not use different names for the same concept in
different modules.

## Dates and audit columns

Every table must carry:

- `created_at`
- `updated_at`

Both fields use `TIMESTAMPTZ NOT NULL DEFAULT NOW()`.

Business dates without a time component must use `DATE`.

For Inven-Track, examples include:

| Field | Type | Purpose |
|---|---|---|
| `entry_date` | `DATE` | Date on which the lot entered the inventory |
| `expiration_date` | `DATE` | Date on which the lot expires |
| `created_at` | `TIMESTAMPTZ` | Date and time when the record was created |
| `updated_at` | `TIMESTAMPTZ` | Date and time when the record was last updated |

The system should use the business date for expiration calculations and
inventory rules, while audit timestamps are used to determine when a record
was created or modified.

## Inventory and lot data

A lot must maintain enough information to support FIFO and expiration
control.

At minimum, the lot data model must be able to identify:

- The product.
- The lot code.
- The initial quantity.
- The available quantity.
- The entry date.
- The expiration date.
- The supplier.
- The purchase price.
- The current status.

The available quantity must be updated when inventory movements are
registered.

FIFO operations must use the lot entry date to determine which available
lot is consumed first, while expiration information is used to generate
inventory alerts.

## Audit and traceability

Inventory operations must preserve information that allows the system to
determine who performed a movement and when it occurred.

Inventory movement records should therefore include a reference to the
responsible user and the operation timestamp.

The system must not delete historical inventory movements as part of normal
inventory operations. Corrections should be represented through a new
controlled movement or adjustment according to the business rules defined
for Inven-Track.

---

**Related:** [`../02-domain/module-boundaries.md`](../02-domain/module-boundaries.md) · [`../02-domain/entities-and-rules.md`](../02-domain/entities-and-rules.md)
