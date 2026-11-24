# Candidate Management Service – Design Document

### Overview

The Candidate Management Service is a Spring Boot backend application designed to store, manage, and search candidate profiles and their associated skills
It provides standard CRUD capabilities alongside flexible, multi-field dynamic filtering, backed by an in-memory H2 database and version-controlled schema migrations.

### Tech Stack & Architecture

**Framework:** Spring Boot (Web, Data JPA)

**Database:** H2 (In-Memory Relational Database - to speed up development. Also can be switched to PostgreSQL in upper envs)

**Schema Management:** Liquibase

**Containerization:** Docker

**CI/CD:** GitHub Actions

### Database Design & Migrations

Schema Management (Liquibase) - 
Database schemas are created and maintained using Liquibase rather than JPA auto-DDL. This ensures repeatable, versioned, and auditable schema changes across environments (In future).

Entity Relationship

The database consists of two primary tables with a One-to-Many / Many-to-One relationship:

candidate: 
Stores core candidate details (Id [Primary Key], Name, E-Mail, City, Total Experience, Notice Period, Created At).

candidate_skills: 
Stores individual skills associated with candidates (Id [Primary Key], candidate_id [Foreign Key], Skill). It contains a foreign key referencing candidate.id, mapped via a @ManyToOne relationship in JPA.

Chose @ManyToOne relationship - If in case of @OneToMany, even with a @JoinColumn, Inserting a new skill triggers two statements — an INSERT into candidate_skills with candidate_id = NULL, followed by an UPDATE to set the foreign key. This also forces you to drop the NOT NULL constraint on candidate_id.

### API Design

CRUD Operations

Standard REST endpoints are exposed to manage the lifecycle of candidate records in the H2 database:

Create Candidate: Adds a new candidate along with their skills.

Retrieve Candidate(s): Fetches candidate details by ID or lists existing records.

Update Candidate: Modifies existing candidate details and associated skills.

Delete Candidate: Removes a candidate record (and handles associated skill records accordingly).

Dynamic Search & Filtering (Specification)

To support flexible querying across multiple candidate attributes and skills without writing rigid, combinatorial repository methods, the application uses the Spring Data JPA Specification API:

Incoming query parameters are dynamically evaluated.

Non-null filters are combined programmatically into JPA CriteriaBuilder predicates.

Supports joining across candidate and candidate_skills when filtering by skill attributes.

### Testing Strategy

The application includes an Integration Test Suite built with @SpringBootTest:

Full Context Loading: Boots the Spring application context, runs Liquibase migrations against the H2 database, and exercises the full stack (Controller → Service → Repository → DB).

Endpoint Coverage: Verifies all CRUD operations as well as multi-field search and filter scenarios to ensure end-to-end correctness.

### Containerization & CI/CD

Local Containerization (Dockerfile)

A Dockerfile is included to package and run the Spring Boot application consistently in local environments:

Builds the executable JAR (e.g., via Maven/Gradle) and packages it into a JVM runtime image.

Exposes the application port for local testing and verification.

Continuous Integration (GitHub Actions)

A GitHub Actions workflow is created to run Integration test cases. This was created to run integration tests after github events (like PR merge into main, etc) or quality gate before deployment (in future)