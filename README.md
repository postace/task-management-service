# Task Management Microservice

A Spring Boot microservice for managing tasks (bugs and features) and users. This service provides a RESTful API for creating, reading, updating, and deleting users and tasks, with support for filtering and searching.

## Features

- **Domain Model**
  - User management (username, fullName)
  - Task management with two distinct categories:
    - Bug (severity, stepsToReproduce)
    - Feature (businessValue, deadline)
  - One-to-many relationship between User and Tasks

- **REST API**
  - CRUD operations for Users
  - CRUD operations for Tasks (both Bug and Feature types)
  - Filtering tasks by status and user
  - Text search in task names

- **Technical Features**
  - Spring Boot 3.2.0
  - Spring Data JPA with Hibernate
  - QueryDSL for advanced filtering
  - PostgreSQL database
  - Flyway for database migrations
  - Comprehensive error handling, structured logging
  - API documentation with OpenAPI/Swagger
  - Unit and integration tests
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

### User Management

- `POST /api/users` - Create a new user
- `GET /api/users/{id}` - Get a user by ID
- `GET /api/users` - List all users
- `PUT /api/users/{id}` - Update a user
- `DELETE /api/users/{id}` - Delete a user

### Task Management

- `POST /api/tasks/bugs` - Create a new bug
- `POST /api/tasks/features` - Create a new feature
- `GET /api/tasks/{id}` - Get a task by ID
- `GET /api/tasks` - List all tasks with optional filtering
- `PUT /api/tasks/bugs/{id}` - Update a bug
- `PUT /api/tasks/features/{id}` - Update a feature
- `DELETE /api/tasks/{id}` - Delete a task

### Filtering Tasks

The `GET /api/tasks` endpoint supports the following query parameters:

- `userId` - Filter tasks by assigned user
- `status` - Filter tasks by status (OPEN, IN_PROGRESS, DONE)
- `searchTerm` - Search tasks by name

Pagination is supported with standard Spring parameters:
- `page` - Page number (0-indexed)
- `size` - Page size
- `sort` - Sort field and direction (e.g., `sort=createdAt,desc`)

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

- **Inheritance for Task Types**: Used JPA inheritance with a joined table strategy to model the different task types (Bug, Feature) while maintaining a common base.
- **QueryDSL for Filtering**: Implemented advanced filtering capabilities using QueryDSL for type-safe queries.
- **DTO Pattern**: Separated the API contract (DTOs) from the domain model to provide flexibility and prevent exposing internal details.
- **MapStruct for Mapping**: Used MapStruct for efficient and type-safe mapping between entities and DTOs.
- **Comprehensive Error Handling**: Implemented a global exception handler to provide consistent error responses.
- **Flyway for Migrations**: Used Flyway to manage database schema changes in a versioned and controlled manner.

## Future Improvements

- Add authentication and authorization (e.g., Spring Security with JWT)
- Implement event-driven architecture for task status changes
- Add more advanced search capabilities (full-text search)
- Implement caching for frequently accessed data
- Add metrics and monitoring (e.g., Micrometer, Prometheus)
- Implement rate limiting for API endpoints

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
