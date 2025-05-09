# Task Management Microservice Design

## Domain Model Design

### Entities

1. **User**
   - id (UUID)
   - username (String, unique)
   - fullName (String)
   - email (String, unique)
   - createdAt (Timestamp)
   - updatedAt (Timestamp)

2. **Task (Base Entity)**
   - id (UUID)
   - name (String)
   - description (String)
   - status (Enum: OPEN, IN_PROGRESS, DONE)
   - createdAt (Timestamp)
   - updatedAt (Timestamp)
   - userId (UUID, foreign key)
   - taskType (String, discriminator column)

3. **Bug (extends Task)**
   - severity (Enum: LOW, MEDIUM, HIGH, CRITICAL)
   - stepsToReproduce (String)
   - priority (Enum: LOW, MEDIUM, HIGH)
   - environment (String)

4. **Feature (extends Task)**
   - businessValue (String)
   - deadline (Date)
   - acceptanceCriteria (String)
   - estimatedEffort (Integer)

## Database Design

Using a single-table inheritance pattern with a discriminator column for task types:

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id UUID REFERENCES users(id),
    task_type VARCHAR(20) NOT NULL,
    
    -- Bug specific fields
    severity VARCHAR(20),
    steps_to_reproduce TEXT,
    priority VARCHAR(20),
    environment VARCHAR(100),
    
    -- Feature specific fields
    business_value TEXT,
    deadline DATE,
    acceptance_criteria TEXT,
    estimated_effort INTEGER
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_name ON tasks(name);
CREATE INDEX idx_tasks_task_type ON tasks(task_type);
```

## API Design

### User Endpoints

1. **Create User**
   - `POST /api/v1/users`
   - Request: `{ "username": "jdoe", "fullName": "John Doe", "email": "john@example.com" }`
   - Response: `201 Created` with user object

2. **Get User by ID**
   - `GET /api/v1/users/{id}`
   - Response: `200 OK` with user object or `404 Not Found`

3. **List All Users**
   - `GET /api/v1/users?page=0&size=20`
   - Response: `200 OK` with paginated user list

4. **Update User**
   - `PUT /api/v1/users/{id}`
   - Request: `{ "fullName": "John Smith" }`
   - Response: `200 OK` with updated user or `404 Not Found`

5. **Delete User**
   - `DELETE /api/v1/users/{id}`
   - Response: `204 No Content` or `404 Not Found`

### Task Endpoints

1. **Create Task**
   - `POST /api/v1/tasks`
   - Request:
     ```json
     {
       "name": "Fix login bug",
       "description": "Users unable to login after password reset",
       "userId": "123e4567-e89b-12d3-a456-426614174000",
       "taskType": "BUG",
       "severity": "HIGH",
       "stepsToReproduce": "1. Reset password, 2. Try to login",
       "priority": "HIGH",
       "environment": "Production"
     }
     ```
     or
     ```json
     {
       "name": "Add payment gateway",
       "description": "Integrate Stripe payment processing",
       "userId": "123e4567-e89b-12d3-a456-426614174000",
       "taskType": "FEATURE",
       "businessValue": "Increased revenue stream",
       "deadline": "2023-12-31",
       "acceptanceCriteria": "User can process payments via Stripe",
       "estimatedEffort": 8
     }
     ```
   - Response: `201 Created` with task object

2. **Get Task by ID**
   - `GET /api/v1/tasks/{id}`
   - Response: `200 OK` with task object or `404 Not Found`

3. **List All Tasks**
   - `GET /api/v1/tasks?page=0&size=20&status=OPEN&userId=123e4567-e89b-12d3-a456-426614174000&searchTerm=login`
   - Response: `200 OK` with paginated task list

4. **Update Task**
   - `PUT /api/v1/tasks/{id}`
   - Request: Task object with updated fields
   - Response: `200 OK` with updated task or `404 Not Found`

5. **Delete Task**
   - `DELETE /api/v1/tasks/{id}`
   - Response: `204 No Content` or `404 Not Found`

## Service Layer Design

### User Service
```java
public interface UserService {
    UserDto createUser(CreateUserRequest request);
    UserDto getUserById(UUID id);
    Page<UserDto> getAllUsers(Pageable pageable);
    UserDto updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
}
```

### Task Service
```java
public interface TaskService {
    TaskDto createTask(CreateTaskRequest request);
    TaskDto getTaskById(UUID id);
    Page<TaskDto> getAllTasks(TaskFilter filter, Pageable pageable);
    TaskDto updateTask(UUID id, UpdateTaskRequest request);
    void deleteTask(UUID id);
}
```

### Task Filter (for search/filtering)
```java
public class TaskFilter {
    private String status;
    private UUID userId;
    private String searchTerm;
    // getters/setters
}
```

## Repository Layer Design

Using Spring Data JPA with custom repositories:

```java
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByUserIdAndStatusAndNameContainingIgnoreCase(
        UUID userId, String status, String searchTerm, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE " +
           "(:userId IS NULL OR t.user.id = :userId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:searchTerm IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Task> findByFilter(UUID userId, String status, String searchTerm, Pageable pageable);
}
```

## Error Handling

Create a global exception handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            response.addError(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse("An unexpected error occurred"), 
                                    HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## Migrations

Using Flyway:

```
db/migration/V1__create_users_table.sql
db/migration/V2__create_tasks_table.sql
db/migration/V3__add_indexes.sql
```

## Code Organization

```
src/
├── main/
│   ├── java/com/example/taskmanagement/
│   │   ├── config/
│   │   ├── api/
│   │   │   ├── UserController.java
│   │   │   └── TaskController.java
│   │   ├── domain/
│   │   │   ├── User.java
│   │   │   ├── Task.java
│   │   │   ├── Bug.java
│   │   │   └── Feature.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── exception/
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── ValidationException.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── TaskRepository.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   ├── UserServiceImpl.java
│   │   │   ├── TaskService.java
│   │   │   └── TaskServiceImpl.java
│   │   └── TaskManagementApplication.java
│   └── resources/
│       ├── application.properties
│       └── db/migration/
│           ├── V1__create_users_table.sql
│           ├── V2__create_tasks_table.sql
│           └── V3__add_indexes.sql
└── test/
    └── java/com/example/taskmanagement/
        ├── api/
        ├── service/
        └── repository/
```

## Testing Strategy

1. **Unit Tests**
   - Service layer tests with mocked repositories
   - Controller tests with mocked services
   - Domain model validation tests

2. **Integration Tests**
   - Repository tests with H2 in-memory database
   - API tests with MockMvc
   - End-to-end tests with Testcontainers for PostgreSQL

3. **Test Examples**
   - Test task creation with both bug and feature types
   - Test filtering tasks by status and user
   - Test search functionality
   - Test validation and error handling

## Extension Points

1. **Task Type Extensibility**
   - Use the inheritance pattern to allow for adding new task types (e.g., Enhancement, Epic)
   - Abstract common task behavior in the base Task class

2. **Status Workflow**
   - Easily extensible to add a workflow engine for task state transitions
   - Additional statuses can be added to the enum

3. **API Versioning**
   - API paths include version (v1) to allow for future changes

4. **Event System**
   - Design can be extended to include event publishing for task status changes

5. **Authentication/Authorization**
   - Structure allows for easy addition of Spring Security

This design provides a solid foundation for the Task Management microservice with strong separation of concerns, proper error handling, and extensibility for future requirements. 