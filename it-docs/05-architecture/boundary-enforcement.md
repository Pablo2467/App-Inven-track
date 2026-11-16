# 05 — Boundary Enforcement
## What is being enforced

One rule, in every module: **a module may import another module's `api/`, and
never its `internal/`.** Names come from
[`module-boundaries.md`](../02-domain/module-boundaries.md), folders from
[`module-structure.md`](./module-structure.md).

For Inven-Track, the main modules are:

- `catalog`
- `inventory`
- `users`

The expected dependency direction is:

```text
inventory → catalog/api
inventory → users/api
```

A module must not access another module's internal implementation directly.

For example, `inventory` may request product or supplier information through
the public API of `catalog`, but it must not access files inside
`catalog/internal/`.

## Tool per stack

| Stack | Tool | The rule you write |
|---|---|---|
| Node / JavaScript | **eslint-boundaries** | An element type per folder, then an allow-list of imports between modules |

Inven-Track uses Node.js and JavaScript with Express, so
`eslint-boundaries` will be used to enforce the module boundaries.

The boundary configuration will be maintained in the project's ESLint
configuration.

## One rule per tool

```jsonc
// eslint-boundaries — eslint.config.js
"settings": {
  "boundaries/elements": [
    {
      "type": "api",
      "pattern": "src/*/api/**",
      "capture": ["module"]
    },
    {
      "type": "internal",
      "pattern": "src/*/internal/**",
      "capture": ["module"]
    }
  ]
},
"rules": {
  "boundaries/element-types": [
    "error",
    {
      "default": "disallow",
      "rules": [
        {
          "from": ["api", "internal"],
          "allow": [
            "api",
            [
              "internal",
              {
                "module": "${from.module}"
              }
            ]
          ]
        }
      ]
    }
  ]
}
```

The rule will allow a module to access its own `internal/` implementation and
another module only through its `api/`.

For example:

```text
inventory → catalog/api
inventory → users/api
```

The following dependencies must be rejected:

```text
inventory → catalog/internal
inventory → users/internal
catalog → inventory/internal
users → inventory/internal
```

An example of a forbidden import would be:

```javascript
import ProductRepository
  from "../catalog/internal/persistence/ProductRepository.js";
```

The `inventory` module should instead use the public API:

```javascript
import { CatalogFacade }
  from "../catalog/api/catalogFacade.js";
```

The purpose is to keep the internal implementation of each module private.

## Where it runs

| Stage | Scope | On failure |
|---|---|---|
| Pre-commit hook, optional | Changed files only | The commit is refused locally |
| `boundary check`, defined in [`../10-devops/ci-cd.md`](../10-devops/ci-cd.md) | The whole tree, on every pull request | The build fails; the pull request cannot merge |

The `boundary check` must run before the test stages. A boundary violation
should be detected before the application tests are executed.

The CI/CD configuration must verify that:

- `inventory` can use `catalog/api/`.
- `inventory` can use `users/api/`.
- A module cannot access another module's `internal/`.
- Invalid imports are detected by ESLint.
- The pull request fails when a forbidden dependency is introduced.

## Breaking the rule on purpose

A suppression needs the tool's inline exemption, a comment naming the `HU-NN`
that forced it, and an ADR recording why — see
[`decisions/README.md`](./decisions/README.md).

One with no ADR is a defect.

For Inven-Track, the preferred solution is to extend the public API of the
module instead of accessing its internal implementation.

For example, if `inventory` requires information from `catalog`, the
`catalog/api/` should expose the required operation instead of allowing
`inventory` to access `catalog/internal/`.

Any exception must identify:

- The related user story, such as `HU-01`.
- The modules affected.
- The reason for the exception.
- The corresponding ADR.
- Whether the exception is temporary or permanent.

---

**Related:** [`./module-structure.md`](./module-structure.md) ·
[`../02-domain/module-boundaries.md`](../02-domain/module-boundaries.md)
