# Task Management Microservice

A Spring Boot microservice for managing tasks (bugs and features) and users. This service provides a RESTful API for creating, reading, updating, and deleting users and tasks, with support for filtering and searching.

## Features

- **Domain Model**
  - User Management
    - Core fields: username (unique), fullName
    - Audit fields: createdAt, deletedAt (for soft delete)
    - One-to-many relationship with Tasks
  - Task Management (Single Table Inheritance)
    - Common fields for all tasks:
      - name, description, status (OPEN, IN_PROGRESS, DONE)
      - Audit fields: createdAt, updatedAt, deletedAt
      - Assigned user relationship
    - Bug specific fields:
      - severity (LOW, MEDIUM, HIGH, CRITICAL)
      - stepsToReproduce
      - priority (LOW, MEDIUM, HIGH)
      - environment
    - Feature specific fields:
      - businessValue
      - deadline
      - acceptanceCriteria
      - estimatedEffort (story points)

- **Technical Features**
  - Spring Boot 3.2.0
  - Spring Data JPA with Hibernate
  - QueryDSL for advanced filtering
  - PostgreSQL database
  - Flyway for database migrations
  - Comprehensive error handling, structured logging
  - API documentation with OpenAPI/Swagger
  - Unit and integration tests with TestContainers
  - Docker support

## Prerequisites

- Java 17 or higher
- Maven 3.8+ (or use the included Maven wrapper)
- PostgreSQL 15+ (or Docker for containerized setup)

## Getting Started

### Running with Maven

1. Clone the repository
2. Configure PostgreSQL database (see `application.yml` for default configuration)
3. Build and run the application:

```bash
# Using Maven wrapper
./mvnw clean install
./mvnw spring-boot:run

# Or with Maven installed
mvn clean install
mvn spring-boot:run
```

### Running with Docker

```bash
# Build and start the application and PostgreSQL database
docker-compose up -d

# Stop the containers
docker-compose down

# Stop the containers and remove volumes
docker-compose down -v
```

## API Documentation

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/api/swagger-ui.html
```

The OpenAPI specification is available at:

```
http://localhost:8080/api/api-docs
```

## API Endpoints

  - Users Endpoints:
    - `POST /api/users` - Create a new user
    - `GET /api/users` - List all users
    - `GET /api/users/{id}` - Get user by ID
    - `PUT /api/users/{id}` - Update user
    - `DELETE /api/users/{id}` - Soft delete user
  - Tasks Endpoints:
    - `POST /api/tasks` - Create a new task (supports both Bug and Feature through request body type)
    - `GET /api/tasks` - List all tasks with filtering support:
      - Filter by status (`status=OPEN|IN_PROGRESS|DONE`)
      - Filter by assigned user (`userId={uuid}`)
      - Search by name (`searchTerm=keyword`)
      - Pagination and sorting (`page`, `size`, `sort`)
    - `GET /api/tasks/{id}` - Get task by ID
    - `PUT /api/tasks/{id}` - Update task (supports both Bug and Feature through request body type)
    - `DELETE /api/tasks/{id}` - Soft delete task

## Project Structure

```
src
├── main
│   ├── java
│   │   └── com
│   │       └── seneca
│   │           └── taskmanagement
│   │               ├── api             # REST controllers
│   │               ├── config          # Configuration classes
│   │               ├── domain          # Domain model entities
│   │               ├── dto             # Data Transfer Objects
│   │               ├── exception       # Exception handling
│   │               ├── mapper          # Entity-DTO mappers
│   │               ├── repository      # Data access layer
│   │               └── service         # Business logic
│   └── resources
│       ├── db
│       │   └── migration              # Flyway migration scripts
│       └── application.yml            # Application configuration
└── test                               # Test classes
```

## Testing

### Manual Usage

Once the application is running, go to this url `http://localhost:8080/api/swagger-ui.html` then test it yourself

### Automated Test

The application includes both unit tests and integration tests:

```bash
# Run all tests
./mvnw test

# Run only unit tests
./mvnw test -Dtest=*Test

# Run only integration tests
./mvnw test -Dtest=*IntegrationTest
```

Integration tests use TestContainers to spin up a PostgreSQL database, ensuring that tests run against a real database environment.

## Design Decisions

- **QueryDSL for Filtering**: Implemented advanced filtering capabilities using QueryDSL for type-safe queries.
- **DTO Pattern**: Separated the API contract (DTOs) from the domain model to provide flexibility and prevent exposing internal details.
- **MapStruct for Mapping**: Used MapStruct for efficient and type-safe mapping between entities and DTOs.
- **Comprehensive Error Handling**: Implemented a global exception handler to provide consistent error responses.
- **Flyway for Migrations**: Used Flyway to manage database schema changes in a versioned and controlled manner.

### Single Table Inheritance for Tasks

The task management system uses single table inheritance instead of joined tables for the following reasons:

1. **Performance**: Single table inheritance eliminates the need for joins when querying tasks, resulting in better query performance, especially when retrieving mixed lists of bugs and features.

2. **Simplicity**: All task data is stored in one table, making it easier to:
   - Query and filter tasks regardless of their type
   - Maintain database schema
   - Handle relationships (e.g., with users)

3. **Flexibility**: Adding new task types or fields is straightforward and doesn't require table modifications or complex join operations.

4. **Consistent Auditing**: Having all tasks in one table makes it easier to implement and maintain audit fields (createdAt, updatedAt, deletedAt).

Trade-offs considered:
- Slightly more storage space used due to nullable columns
- Less strict schema constraints compared to joined tables

The benefits of improved query performance and simpler maintenance outweigh these minor drawbacks for our use case.

## Future Improvements

- Add authentication and authorization (e.g., Spring Security with JWT)
- Implement event-driven architecture for task status changes
- Add more advanced search capabilities (full-text search)
- Implement caching for frequently accessed data
- Add metrics and monitoring (e.g., Micrometer, Prometheus)
- Implement rate limiting for API endpoints

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
