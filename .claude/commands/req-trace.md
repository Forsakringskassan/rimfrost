# Requirement traceability — map tests to requirements and add @DisplayName

Analyse the test files in the current project, map each test method to the requirements it
verifies, then add or update `@DisplayName` annotations so the mapping is visible in test reports.

---

## Step 1 — Load requirements

Look for a requirements document in this order:
1. `docs/krav.md` (canonical location for FK repos)
2. Any other `docs/*.md` that contains numbered requirement IDs (FR-NN, NFR-NN, or similar)
3. README or CHANGELOG if no dedicated requirements doc exists

Extract all requirement IDs and their descriptions. Note the numbering scheme (e.g. FR-01.2).

---

## Step 2 — Analyse test files

Use the Explore subagent (thoroughness: very thorough) to read every test file under `src/test/`.

For each test method, determine:
- Which requirement IDs it exercises, matched against the list from Step 1
- The business behaviour being asserted (not the technical assertion — the *why*)

Return a table: test class · test method · requirement IDs · one-line behaviour summary.

---

## Step 3 — Add or update @DisplayName annotations

For each test method identified in Step 2:

1. Add the `@DisplayName` annotation immediately after the last existing annotation and before the method signature.
2. Format: `"<req-ids>: <short Swedish description of what is verified>"`
   - `req-ids`: comma-separated full sub-requirement IDs (e.g. `FR-01.2, FR-01.7`), not top-level IDs alone
   - Description: one concise sentence in the same language as the requirements document
3. Add `import org.junit.jupiter.api.DisplayName;` if not already present.
4. Do not change anything else in the test files.

Run `mvn test-compile -q` after all edits and fix any compilation errors before finishing.

---

## Step 4 — Report coverage gaps

List any requirements from Step 1 that are not referenced by any test method.
