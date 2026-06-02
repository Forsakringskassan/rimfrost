# Fault Handling in Rimfrost — Responsibilities

## Purpose

This document describes **who is responsible for what** in Rimfrost's fault handling:
what the framework handles automatically, what a regel is expected to do itself, and
what mechanisms the framework provides to support it.

For a deeper walkthrough of implementation and code examples, see [FAULT_HANDLING.md](FAULT_HANDLING.md).

---

## 1. Maskinella regler

### Framework responsibilities

The framework (`RegelMaskinellRequestHandler`) orchestrates the full call sequence:
read from Handläggning → execute regel → write result back to Handläggning.

**Handläggning retries.** The framework wraps both the Handläggning read and the Handläggning
write in `RetryUtil` with exponential backoff. A regel never sees a transient Handläggning
failure — it either receives the data it needs or the flow is terminated with a specific error
code (`HANDLAGGNING_READ_FAILURE` / `HANDLAGGNING_WRITE_FAILURE`).

**Read and write retried independently.** If the regel runs successfully but the write back
to Handläggning fails transiently, only the write is retried. The business logic is never
re-executed because of a write failure.

**Last-resort exception catch.** If the regel throws an unhandled exception, the framework
catches it and sends a Kafka error response with `felkod=OTHER`. A regel is expected to
handle its own failures explicitly — this catch exists to prevent silent failures, not as a
substitute for proper error handling.

### Regel responsibilities

**Retries for external adapters.** The framework does not automatically retry calls to
external endpoints other than Handläggning. A maskinell regel is responsible for wrapping
calls to adapters such as Folkbokföring and Arbetsgivare in `RetryUtil` itself.

**RetryUtil empty() vs. of(value)** Inside a `RetryUtil` supplier, the regel
should return `Result.empty()` e.g. to signal a transient failure (that could be triggering a retry) and `Result.of(value)`
to signal success. Exceptions thrown by the adapter are caught internally and treated as
`Result.empty()`.

**Business-level errors.** When the regel itself determines that a case must be rejected for
business reasons — not due to an infrastructure problem — it returns a `RegelMaskinellErrorResult`
directly. 

### Mechanisms

**`RetryUtil`** wraps a supplier around e.g. an adapter call and executes it with
exponential backoff. The retry schedule is configured per environment — short intervals in
test, up to several hours in production.

**`Result.of(value)` / `Result.empty()`** is the contract between the regel and `RetryUtil`.
Inside the supplier the regel returns `Result.of(value)` on success and `Result.empty()` to
signal that the call failed transiently and could be retried. When retries
are exhausted, `RetryUtil` throws `RetriesExhaustedException.

**`RegelMaskinellErrorResult`** is used when the regel itself decides a case cannot proceed
— not due to an infrastructure problem, but for a business reason.

**`RegelFelkod`** provides string definitions for error codes used in both `RegelMaskinellErrorResult`
and the framework's own error responses.

---

## 2. Manuella regler

### Framework responsibilities

Manuella regler actions are triggered in two ways: Kafka-triggered and
REST-triggered. The framework manages both.

If a critical step in a Kafka triggered action fails, the
framework catches the error, attempts to end any OUL operativ uppgift that was already
created (fire-and-forget), and sends a structured error response. Steps after the failure
are never attempted — no partial state is written. Unhandled exceptions from the regel are
also caught here and result in a Kafka error response with `felkod=RIMFROST_OTHER`.

If a REST-triggered step fails: The framework maps exceptions from the call of the endpoint
to structured HTTP responses via e.g. `RegelManuellExceptionMapper`.

**Cleanup failure isolation.** The Kafka success response is sent before cleanup runs. The
framework then performs a fixed set of cleanup steps — deleting stored correlation data and
cloud event data, and updating Handläggning. These are not customisable by the regel.

### Regel responsibilities

When a step fails in a way that makes it impossible to continue, the
regel throws appropriate exception. This aborts the current operation and sends an error response back to the REST-caller.
