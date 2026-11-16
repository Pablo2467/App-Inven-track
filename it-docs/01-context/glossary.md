# 01 — Glossary

## Terms

| **Term** | **Definition** | **Forbidden synonyms** |
| ------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------- | -------------------------------- |
| Product | An item registered in Iven-Track and managed as part of the inventory | Item, article |
| Category | A named grouping used to organize products | Tag, label |
| Inventory | The collection of products and quantities currently managed by the system | Warehouse |
| Stock | The quantity of a product currently available in the inventory | Inventory |
| Stock entry | A record representing products or quantities entering the inventory | Stock input |
| Stock exit | A record representing products or quantities leaving the inventory | Stock output |
| Stock batch | A quantity of a product that entered the inventory at the same time and is tracked for FIFO purposes | Group |
| FIFO | First In, First Out; the inventory method that requires stock that entered first to be considered for exit before newer stock | LIFO |
| Inventory movement | A change in inventory caused by a stock entry or stock exit | Transaction |
| User | A person authorized to access and operate Iven-Track | Customer, client |
| Module | A bounded context inside the single deployable application | Service |
| Available quantity | The amount of a product or stock batch that can currently be removed from inventory | Remaining stock |

## Why forbidden synonyms matter

Two words for the same thing let two documents describe the same rule differently and both look correct. When a reviewer sees a forbidden synonym in a pull request, the fix is to rename the usage, not to add the synonym here — unless the row above already defines a distinct meaning for that word.

## Adding a term

A term is added here the first time it appears in a second document. A term used in exactly one document belongs in that document, not here.

---

**Related:** [`./overview.md`](./overview.md) · [`../02-domain/module-boundaries.md`](../02-domain/module-boundaries.md)