# Reverse-engineer service requirements

Explore the current service and produce `docs/krav.md` — functional and non-functional requirements with numbered IDs.

If the file already exists, update it rather than replacing it.

---

## Step 1 — Explore the codebase

Use the Explore subagent (thoroughness: very thorough) to investigate:

- All REST endpoints (`@Path`, `@GET`, `@POST`, etc.) — paths, request/response bodies, status codes
- All Kafka channels (`@Incoming`, `@Outgoing`, topic names, message schemas)
- All domain model classes and DTOs — fields, types, nullability
- application.properties / application.yml — integration URLs, topic names, env vars
- Any existing OpenAPI or AsyncAPI spec artefacts referenced in pom.xml
- Test files — to infer expected behaviour and edge cases
- CHANGELOG.md — to understand the evolution of the service

Report back: purpose, actors, all endpoints, all topics, domain model, notable business logic, known limitations.

---

## Step 2 — Write `docs/krav.md`

Write in Swedish. Structure:

1. **Bakgrund och syfte** — what the service does and why it exists
2. **Intressenter och aktörer** — table of actors and their roles
3. **Funktionella krav** — one `### <PREFIX>-FR-NN — <name>` section per capability, with sub-requirements numbered `<PREFIX>-FR-NN.M` as bold prefixes on bullet points
4. **Uppgiftsstatus / Statusmodell** — if the service has a status lifecycle, document it as a table
5. **Icke-funktionella krav** — one `### <PREFIX>-NFR-NN — <name>` section per concern, sub-requirements numbered `<PREFIX>-NFR-NN.M`
6. **API-gränssnitt (översikt)** — table listing each API, its audience, and its spec artefact name; no schema details
7. **Integration med [X]** — brief note on upstream/downstream integration context

Rules:
- Do not inline request/response schemas — those belong in the OpenAPI spec
- Do not reference implementation technology (Quarkus, JAX-RS, etc.) — this is a requirements doc
- Derive a short, unique prefix (3–5 uppercase letters) from the service/repo name and use it as a namespace for all requirement IDs — e.g. `FRMA` for `rimfrost-framework-regel-maskinell`, `FRMM` for `rimfrost-framework-regel-manuell`. Suggest the prefix to the user before writing and wait for confirmation.
- FR IDs must be stable — once written, do not renumber
- Keep each sub-requirement to one sentence

