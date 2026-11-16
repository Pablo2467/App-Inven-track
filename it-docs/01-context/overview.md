# 01 — Overview

## What it does

Iven-Track is a web application for managing inventory and controlling the movement of products. It allows users to register products, manage stock entries and exits, and maintain accurate inventory levels through the FIFO (First In, First Out) method.

| **Capability** | **Description** |
| ------------------------- | ------------------------------------------------------------- |
| Product management | Register, update, categorize, and manage products |
| Stock entry management | Register incoming quantities and their entry information |
| Stock exit management | Register outgoing quantities from the inventory |
| FIFO inventory management | Ensure that stock that entered first is considered for exit first |
| Inventory control | Track available quantities and product movements |
| Inventory consultation | View current stock and product availability |

## Who it is for

| **Audience** | **Uses it to** |
| ---------------------- | ------------------------------------------------------------- |
| Inventory staff | Register products and manage stock entries and exits |
| Store employees | Consult available inventory and register product movements |
| Store manager | Monitor inventory levels and product movements |

## Problem it solves

Before Iven-Track, inventory management may rely on manual records or spreadsheets, making it difficult to maintain accurate stock information and determine which products should leave the inventory first.

Iven-Track centralizes inventory information and applies the FIFO (First In, First Out) method to ensure that stock that entered the inventory first is considered for removal before newer stock. This helps maintain an organized and traceable inventory process.

## Constraints

| **Constraint** | **Why** |
| ------------------------- | ------------------------------------------------------------- |
| Single deployable application | The system is designed as one application for centralized inventory management |
| Relational database | Product information and inventory movements require structured and consistent data storage |
| FIFO inventory method | Stock exits must follow the First In, First Out rule |
| Authorized users | Inventory operations are performed by registered users of the system |

---

**Related:** [`./scope.md`](./scope.md) · [`./glossary.md`](./glossary.md)