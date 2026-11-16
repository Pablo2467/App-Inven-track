# 06 — Migrations

## One sequence for the whole application

Inven-Track uses one database, so there is one ordered changelog for the
entire application. Database changes from different modules must follow the
same version sequence.

For example, a migration that changes the `catalog` schema and another that
changes the `inventory` schema must belong to the same ordered migration
history.

All migration files must be stored under:

`resources/db/migration/`

The migration history must not be separated into independent sequences per
module.

| Rule | Detail |
|---|---|
| Tool | Liquibase |
| Sequence | One changelog with one increasing version sequence for the entire database |
| Changeset scope | One logical database change per changeset |
| Changeset history | A changeset that has already been executed must never be edited |
| Module tag | Each changeset id is prefixed with its owning module, for example `inventory-003`, for traceability only |
| Execution order | Changesets are executed according to the global database sequence |

## Module ownership

Each migration must identify the module responsible for the database object
being changed.

For Inven-Track, the main module prefixes are:

| Module | Example changeset | Purpose |
|---|---|---|
| `catalog` | `catalog-001` | Products and suppliers |
| `inventory` | `inventory-001` | Lots, entries, outputs and inventory movements |
| `users` | `users-001` | Application users and responsible users |

The module prefix provides traceability but does not create a separate
migration sequence.

For example:

`catalog-001`  
`inventory-001`  
`inventory-002`  
`users-001`

The numbers represent the global order in which database changes are
applied.

## Rollback policy

| Change type | Rule |
|---|---|
| Additive — new nullable column, new table or new index | Must include an automatic or straightforward rollback |
| Destructive — drop column, drop table or rename | Must be performed in two phases: first deploy the replacement and update the application, then remove the old structure in a later migration |
| Data transformation | Must define how the data can be restored or safely recovered |
| Any changeset | Must define a working `rollback` and have it verified before merge |

A changeset without a valid rollback is considered incomplete.

Rollback testing must verify that the database can return to the state that
existed before the migration.

## Changeset rules

Each changeset must represent one logical database modification.

Examples of valid changesets include:

- Create the `products` table.
- Create the `suppliers` table.
- Create the `lots` table.
- Add the `expiration_date` column to `lots`.
- Create an index for `product_id`.
- Add a new inventory movement field.

A changeset that has already been executed in an environment must not be
modified. Any correction must be introduced through a new changeset.

## Inven-Track migration order

The initial database structure should follow the dependencies between the
main concepts of the system.

A possible initial sequence is:

1. Create users structure.
2. Create products structure.
3. Create suppliers structure.
4. Create lots structure.
5. Create inventory entries structure.
6. Create inventory outputs structure.
7. Create inventory movements structure.
8. Add indexes and additional constraints required by the application.

The final order must respect the dependencies defined in the domain model
and database conventions.

## FIFO and migration considerations

The database structure must support the FIFO inventory logic defined by
Inven-Track.

The lot structure must preserve the information required to determine which
lot should be consumed first, including the product, available quantity and
entry date.

Migrations must not remove or alter historical inventory information in a
way that prevents the system from determining previous movements or lot
balances.

Changes affecting FIFO-related data must be reviewed to ensure that existing
inventory records remain consistent.

## Migration verification

Before a migration is merged, it must be verified to ensure that:

- The migration can be executed successfully.
- The resulting database structure matches the expected model.
- The rollback works correctly.
- Existing inventory data is not unintentionally modified or deleted.
- FIFO-related information remains available.
- The migration follows the naming and module ownership conventions.

---

**Related:** [`./database-conventions.md`](./database-conventions.md) · [`../05-architecture/module-structure.md`](../05-architecture/module-structure.md)
