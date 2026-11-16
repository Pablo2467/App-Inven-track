# 07 — REST Conventions

> [!NOTE] INSTRUCTIONS
> This governs the one HTTP API the application exposes — not a module's
> in-process public API, which is a different thing defined in
> `module-boundaries.md`. Delete this block once an endpoint ships using it.

## One API, not one per module

The application exposes exactly one HTTP API to its clients. A module's
public API is an in-process boundary between modules, defined in
[`module-boundaries.md`](../02-domain/module-boundaries.md) — it is never
called over HTTP, and it does not get its own version number. Versioning
below applies once, to the whole application.

| Element | Convention | Example |
|---|---|---|
| Base path | `https://localhost:3000/api/v1` | `/api/v1` |
| Version bump | A breaking change to any endpoint's request or response bumps `{n}` for the whole API | `/api/v1` → `/api/v2` |

## Resource naming

| Rule | Example |
|---|---|
| Plural nouns, not verbs | `/api/v1/products`, not `/api/v1/getProducts` |
| Resource identifiers use path parameters | `/api/v1/products/{productId}` |
| kebab-case in multi-word paths | `/api/v1/stock-entries` |

## Status codes

| Code | When |
|---|---|
| 200 / 201 | Success reading, updating, or creating a resource |
| 400 / 422 | The request is malformed, or well-formed but fails an Inven-Track business rule |
| 401 / 403 | No valid credentials, or credentials without permission |
| 404 | The requested product, supplier, batch, inventory record, movement, or user does not exist |
| 409 | The request conflicts with the resource's current state — e.g. a stock exit cannot be completed because there is insufficient available inventory |
| 500 | The application failed to handle the request because of an unexpected internal error |

## Error format

```json
{ "error": "VALIDATION_ERROR", "message": "quantity must be greater than 0",
  "details": [{ "field": "quantity", "message": "must be greater than 0" }] }
