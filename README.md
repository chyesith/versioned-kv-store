# Versioned Key-Value Store

[![CI](https://github.com/chyesith/versioned-kv-store/actions/workflows/pipeline.yml/badge.svg)](https://github.com/chyesith/versioned-kv-store/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=versioned-kv-store&metric=alert_status)](https://sonarcloud.io/dashboard?id=versioned-kv-store)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=versioned-kv-store&metric=coverage)](https://sonarcloud.io/dashboard?id=versioned-kv-store)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=versioned-kv-store&metric=bugs)](https://sonarcloud.io/dashboard?id=versioned-kv-store)

A **production-grade, version-controlled key-value store** built with Spring Boot.  
This system supports **concurrent-safe updates**, **full audit history**, and **point-in-time reads**, designed to behave correctly under real-world load and race conditions.

---

##  Live API

- **Base URL:** https://versioned-kv-store.onrender.com
- **Swagger UI:** https://versioned-kv-store.onrender.com/swagger-ui.html

---

##  Features

- Version-controlled key-value storage
- Full audit history (immutable version records)
- Point-in-time reads using UNIX timestamps
- Strong consistency under concurrent updates
- RESTful API using GET and POST
- PostgreSQL-backed persistence
- CI/CD with automated testing and quality checks

---

##  Architecture

Client → Controller → Service → Repository → PostgreSQL

The system follows a layered architecture with clear separation of concerns:
- Controllers handle HTTP requests
- Services enforce business logic and concurrency control
- Repositories manage database interactions

---

##  Data Model

**Record (Current State)**
- record_id (Primary Key)
- key_name (Unique)
- value
- current_version
- created_at

**RecordVersion (Audit History)**
- id (Primary Key)
- record_id (Foreign Key)
- version
- value
- created_at

The design uses an **append-only version table** to ensure full historical traceability.

## CI/CD Pipeline

This project uses GitHub Actions for a production-grade CI/CD pipeline.

Pipeline: Java CI/CD production pipeline

Triggers

- Push to main
- Pull requests to main

Build & Test Stage
- Runs on ubuntu-latest
- Uses PostgreSQL service container
- Sets up JDK 21 (Temurin)
- Caches Maven dependencies


   Build, Test & Code Analysis
------------------------------

Executed via Maven:

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   ./mvnw -B verify sonar:sonar   `

Includes:

*   Compilation

*   Unit & integration tests

*   JaCoCo coverage generation

*   SonarCloud analysis


 Code Quality (SonarCloud)
----------------------------

*   Quality Gate enforcement

*   Bug detection

*   Coverage tracking

*   Static code analysis


 Coverage Reporting (JaCoCo)
------------------------------

Pipeline automatically:

*   Generates XML + HTML reports

*   Uploads HTML report as artifact

*   Publishes summary in GitHub Actions UI


### Metrics include:

*   Line coverage

*   Branch coverage

*   Method coverage

*   Class coverage

*   Instruction coverage


 Pull Request Coverage Feedback
---------------------------------

*   Automatically comments on PRs


### Enforces:

*   Minimum overall coverage: **70%**

*   Minimum changed files coverage: **70%**


 Artifacts
------------

*   JaCoCo HTML report (downloadable)

*   Coverage summary in workflow


 Deployment
-------------

*   Automatically triggered after successful CI

*   Deployed to **Render**