# Changelog

## [1.0.0] - 2026-04-21

### Features
* **New Endpoint:** Implemented `GET /object/get_all_records` to retrieve all key-value state.
* **Security & Validation:** Added input validation for request payloads and implemented basic rate limiting to prevent abuse.
* **Observability:** Added `RequestLoggingFilter` with MDC (Mapped Diagnostic Context) for unique request tracing.

### Fixes
* **Concurrency:** Resolved critical race condition during record insertion.
    * Implemented Pessimistic Locking (`PESSIMISTIC_WRITE`) and `REQUIRES_NEW` transaction propagation for robust thread safety.
    * Verified through custom `KvStoreFloodTest` (50 concurrent request simulation).

### Infrastructure
* **CI/CD Pipeline:** Configured GitHub Actions pipeline (`pipeline.yaml`) for automated Build, Test, and Analysis.
* **Code Quality:** Integrated SonarCloud with mandatory Quality Gate (70% coverage enforcement).
* **Testing:** Implemented Testcontainers for real-world PostgreSQL integration testing.
* **Documentation:** Professionalized `README.md` and added full OpenAPI/Swagger documentation.