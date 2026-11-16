# 00 — Agile Conventions

This project runs two-week sprints on a single backlog for a single deployable
application built by one team.

## Ceremonies

| Ceremony | Cadence | Artifact produced |
|---|---|---|
| Backlog refinement | Weekly, 1 hour | Stories move to "Ready" according to [`definition-of-ready.md`](./definition-of-ready.md) |
| Sprint planning | Every 2 weeks, 2 hours | Sprint goal and committed story list |
| Daily standup | Daily, 15 minutes | Blockers and current work status recorded in the tracker |
| Sprint review / demo | Every 2 weeks, 1 hour | Stakeholder feedback recorded on the story |
| Sprint retrospective | Every 2 weeks, 1 hour | Completed sprint retrospective based on [`_template-sprint-retro.md`](./_template-sprint-retro.md) |
| Backlog grooming | Every 2 weeks, 30 minutes | Reprioritized backlog and updated estimates |

## Estimation

- **Unit:** story points, using a Fibonacci-like scale (1, 2, 3, 5, 8, 13).
- **Who estimates:** the whole team through planning poker.
- **Re-estimation:** allowed only before a story enters "Ready."

## Backlog tool

The backlog uses a single board with the following columns:

`Backlog -> Ready -> In progress -> In review -> Done`

A story does not skip a column, including small changes.

---

**Related:** [`definition-of-ready.md`](./definition-of-ready.md) · [`README.md`](./README.md)
```

### `definition-of-ready.md`

```md
# 00 — Definition of Ready

A story does not enter sprint planning until every row below is true.

## Checklist

| # | Criterion | How it is verified |
|---|---|---|
| 1 | The story has a single, testable set of acceptance criteria | Written as Given/When/Then in the story description |
| 2 | The story fits in one sprint | Estimated at 8 points or less during planning poker |
| 3 | Dependencies on other stories are identified | Dependencies are linked in the tracker |
| 4 | The module affected by the story is named | Checked against [`../09-modules/module-catalog.md`](../09-modules/module-catalog.md) |
| 5 | UX is attached when the story changes a screen | Wireframe or design link is attached to the story |
| 6 | No open question is left for the developer to decide alone | No unresolved product questions remain |
| 7 | The story does not silently widen scope | The Product Owner or team has stated the story in one sentence |

## What "Ready" is not

Ready does not mean that implementation details have already been designed.

It means the team can start the story without stopping to determine what the
business requirement actually means.

---

**Related:** [`definition-of-done.md`](./definition-of-done.md) · [`README.md`](./README.md)
```