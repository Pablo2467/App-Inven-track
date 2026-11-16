## 2. `02-domain/entities-and-rules.md`

# 02 — Entities and Rules

## Entities

| Entity | Key attributes | Invariants |
|---|---|---|
| Product | id, name, sku, active | The SKU must be unique; a product cannot have a negative available stock |
| StockEntry | id, product, quantity, remaining quantity, entry date | Quantity must be greater than zero; remaining quantity cannot be negative or exceed the original quantity |
| StockExit | id, product, quantity, exit date | Quantity must be greater than zero and cannot exceed the available stock |

## Business rules

1. **BR-01** — A stock entry must reference an existing product.

2. **BR-02** — A stock entry quantity must be greater than zero.

3. **BR-03** — A stock exit must reference an existing product.

4. **BR-04** — A stock exit quantity must be greater than zero and cannot exceed the product's available stock.

5. **BR-05** — Stock exits must consume the oldest available stock entries first according to their entry date.

6. **BR-06** — A stock entry with remaining quantity equal to zero cannot provide additional stock for future exits.

7. **BR-07** — The available stock of a product is equal to the sum of the remaining quantities of its stock entries.

8. **BR-08** — Every stock entry and stock exit must be recorded as an inventory movement.

## Where a rule is enforced

Every `BR-NN` rule is enforced in the `inventory` module because that module owns the Product, StockEntry, and StockExit entities and the FIFO inventory behavior.

A rule enforced outside the inventory module is a sign that the module boundary should be reconsidered.

---

**Related:** [`./domain-map.md`](./domain-map.md) · [`./module-boundaries.md`](./module-boundaries.md)
