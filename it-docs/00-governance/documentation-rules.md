# 00 — Documentation Rules

This document defines when a document in the Iven-Track repository is
considered complete and how project documentation must be maintained.

## Document lifecycle

| State | Meaning | Marker |
|---|---|---|
| Draft | Content is being prepared | Instructions may still be present |
| Review | Content is complete and awaiting review | No unresolved project decisions |
| Done | Content has been reviewed and accepted | Instructions block removed |

A document is not considered Done only because it contains text. It must also
represent the current project and be reviewed when required.

## Rules

1. **One question per document.** A document should answer one main question. If
   it covers two independent topics, split it.

2. **Avoid invented project facts.** Documentation must describe decisions,
   behaviors, and tools that actually belong to Iven-Track.

3. **Keep terminology consistent.** Domain terms must use the definitions in
   [`../01-context/glossary.md`](../01-context/glossary.md).

4. **Use tables and diagrams when they communicate structure better than prose.**
   Prose should mainly explain decisions and their reasoning.

5. **Use relative links.** Documentation links must remain valid when the
   repository is copied or forked.

6. **English is the repository working language.** A domain term should keep the
   same meaning throughout the documentation.

7. **Documentation changes are versioned with the code.** When implementation
   changes invalidate documentation, the documentation must be updated in the
   same pull request whenever practical.

## Review checklist

- [ ] The document answers its intended question
- [ ] The content matches the current Iven-Track project
- [ ] No invented functionality or unresolved placeholders remain
- [ ] Domain terminology is consistent with `01-context/glossary.md`
- [ ] Relative links resolve
- [ ] Diagrams render correctly on GitHub
- [ ] The document has been reviewed when review is required
- [ ] The instructions block has been removed when the document is Done

---

**Related:** [`../01-context/glossary.md`](../01-context/glossary.md) · [`README.md`](./README.md)
