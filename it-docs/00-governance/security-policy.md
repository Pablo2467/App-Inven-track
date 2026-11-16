# 00 — Security Policy

This project ships as a single deployable application. Security controls apply
to the application, its source code, database, and deployment environment.

## Secrets management

| Rule | Detail |
|---|---|
| No secrets in source control | Passwords, API keys, tokens, and private credentials must not be committed |
| Local development | Sensitive configuration is provided through environment variables or a local `.env` file excluded from Git |
| Shared environments | Production and deployment secrets are configured outside the source code |
| Secret exposure | Exposed credentials must be revoked or replaced as soon as possible |

## Dependency and input security

- Project dependencies must be reviewed before being added.
- Known vulnerable dependencies should be updated or removed.
- User input must be validated before it reaches business logic or persistence.
- Database operations must use parameterized queries or the application's ORM mechanisms.
- Sensitive information must not be written to application logs.

## Authentication and authorization

If authentication is enabled in Iven-Track:

- Protected operations must require an authenticated user.
- Authorization rules must be enforced by the backend and must not depend only on frontend controls.
- Users must only perform operations allowed by their assigned permissions or role.
- Passwords must never be stored in plain text.

## Database security

- Database credentials must not be committed to the repository.
- The application should use a database account with only the permissions required for normal operation.
- Database schema changes must be versioned and reproducible.
- Production and development credentials must remain separate.

## Application security

- Product, stock entry, and stock exit operations must validate their input.
- Stock quantities must never be allowed to become negative through invalid requests.
- FIFO processing must validate that a stock exit does not consume more stock than is available.
- Error responses must not expose credentials, database details, or internal secrets.

## Incident response

A suspected credential leak, unauthorized access, or security vulnerability must
be reported to the team as soon as it is discovered.

Exposed credentials must be revoked or replaced before normal operation resumes.

---

**Related:** [`documentation-rules.md`](./documentation-rules.md) · [`README.md`](./README.md)