# 12 — Design System

## Tokens

### Color

| Token | Value | Use |
|---|---|---|
| `--color-primary` | `#0B5FFF` | Primary actions: save product, confirm entry, confirm output |
| `--color-danger` | `#D1293D` | Destructive actions: disable a user; blocking errors, e.g. "Not enough stock available" |
| `--color-success` | `#1E824C` | Confirmation banners: entry recorded, output recorded, product registered |
| `--color-warning` | `#B7791F` | Non-blocking alerts: products near expiration, low stock below minimum |
| `--color-text` | `#1A1A1A` | Default body text |
| `--color-bg-surface` | `#FFFFFF` | Card and form backgrounds |

### Typography and spacing

| Token | Value | Use |
|---|---|---|
| `--font-size-h1` | `1.75rem` / 700 weight | Page titles, e.g. "Register product" |
| `--font-size-body` | `1rem` / 400 weight | Table rows, labels, body copy |
| `--font-size-caption` | `0.8125rem` / 400 weight | Field hints, lot expiration dates, timestamps |
| `--space-unit` | `4px` | Base of the spacing scale: 4, 8, 16, 24, 32px |

## Component inventory

| Component | Used by | Notes |
|---|---|---|
| Button (primary / secondary / danger) | Every screen | One primary button per screen, at most |
| Data table | Products near expiration, inventory movements, manage users | Paginated, sortable by column |
| Form field with inline validation | Register product, register supplier, register entry, register output | Error message states the fix, not just the failure |
| Empty state | Inventory movements, manage users | Message plus the one action that fills it |
| FIFO lot preview (read-only list) | Register inventory output | Shows oldest-lot-first consumption before the user confirms; never directly editable |
| Status badge | Manage users, products near expiration | e.g. "Disabled" on a user row, expiration urgency on a lot row |

## Accessibility target

Target level: **WCAG 2.1 AA**.

| Aspect | Requirement |
|---|---|
| Contrast | 4.5:1 for normal text, 3:1 for large text and UI controls |
| Keyboard | Every interactive element reachable and operable without a mouse |
| Labels | Every form field has a programmatically associated label |

---

**Related:** [`./README.md`](./README.md) · [`./_template-screen.md`](./_template-screen.md)
