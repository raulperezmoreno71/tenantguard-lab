# TenantGuard Lab

TenantGuard Lab is a Java 21 / Spring Boot REST API that I developed from scratch for studying authorization in a multi-tenant system. Users belong to organizations through memberships with `ADMIN`, `EDITOR`, or `VIEWER` roles; documents belong to one organization. The project is an evolving AppSec portfolio, not a production service.

The API uses Spring Security with RSA-signed JWTs, Spring Data JPA, PostgreSQL 17, Flyway, Maven, and Docker Compose. JUnit and MockMvc cover authorization behavior; Kali Linux and Burp Suite were used for the manual lab exercise.

## Security labs

| Lab | Topic | Case study | Exercise branch |
| --- | --- | --- | --- |
| 01 | Cross-tenant Broken Object Level Authorization (BOLA / IDOR) | [Read the case study](docs/labs/01-cross-tenant-bola/README.md) | [`lab/bola`](https://github.com/raulperezmoreno71/tenantguard-lab/tree/lab/bola) |
| 02 | Broken Function Level Authorization (BFLA): vertical privilege escalation | [Read the case study](docs/labs/02-vertical-privilege-escalation/README.md) | [`lab/bfla`](https://github.com/raulperezmoreno71/tenantguard-lab/tree/lab/bfla) |

`main` is the secure reference implementation and contains the documentation and regression tests. Each `lab/...` branch is a separate, intentionally vulnerable exercise; **never merge a vulnerable branch into `main`**. Vulnerable branches are for isolated local education only—do not deploy them publicly or use real data or credentials.

## Run locally

- [Development: Spring Boot in IntelliJ, PostgreSQL in Docker](LAB.md) — see “Development”
- [Lab: Spring Boot and PostgreSQL in Docker](LAB.md) — see “Laboratory”
- [API routes and permission policy](API.md)

The Compose environments use separate database volumes. Keep `.env`, `.env.lab`, and RSA keys local; they are ignored by Git. Follow `LAB.md` for the required variables, key mounts, startup commands, and safe shutdown procedure. The lab is bound to `127.0.0.1:8080` and should not be exposed beyond the local machine.
