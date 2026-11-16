# 00 — Git Conventions

This document defines the Git workflow used by the Iven-Track team.

## Branch strategy

```text
main
 |
 |-- dev
      |
      |-- feat/[description]
      |-- fix/[description]
      |-- chore/[description]
      |-- hotfix/[description]
````

* `main` is the stable branch and should remain deployable.
* `dev` is the integration branch for completed feature work.
* Feature branches are created from `dev`.
* Bug-fix branches are created from `dev`.
* Documentation, configuration, and tooling changes use `chore`.
* Urgent production fixes use `hotfix` from `main`.
* Nobody commits directly to `main` or `dev`.
* One branch represents one task or user story.
* Branches are deleted after they are merged.

## Branch naming

Use:

`[type]/[short-description-in-kebab-case]`

Examples:

* `feat/register-product`
* `feat/register-stock-entry`
* `feat/process-stock-exit`
* `feat/apply-fifo`
* `fix/negative-stock-validation`
* `docs/update-domain-map`
* `chore/update-dependencies`

## Commit format

The project uses Conventional Commits.

| Type       | When to use                                          |
| ---------- | ---------------------------------------------------- |
| `feat`     | New functionality                                    |
| `fix`      | Bug fix                                              |
| `docs`     | Documentation changes                                |
| `refactor` | Internal restructuring without changing behavior     |
| `test`     | Add or modify tests                                  |
| `chore`    | Tooling, dependencies, configuration, or maintenance |
| `perf`     | Performance improvement                              |

Format:

`type(scope): imperative, lowercase, no trailing period`

Examples:

* `feat(inventory): register product`
* `feat(inventory): record stock entry`
* `feat(inventory): process fifo stock exit`
* `fix(inventory): prevent negative stock`
* `docs(domain): update fifo rules`

The scope should normally identify the affected module, such as `inventory`.

## Merge policy

* Feature, fix, documentation, and chore branches are merged into `dev` through pull requests.
* `dev` is merged into `main`
