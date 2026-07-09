# EduCore360

A role-based academic management REST API built with **Spring Boot 3**, **Spring Security (JWT)**, **JPA/Hibernate**, and documented with **Swagger/OpenAPI**.

---

## API Base Paths

| Role              | Base Path                        |
|-------------------|----------------------------------|
| User (public)     | `/api/v1/user`                   |
| Student           | `/api/v1/student`                |
| Instructor        | `/api/v1/instructor`             |
| Registrar         | `/api/v1/registrar`              |
| Exam Coordinator  | `/api/v1/exam-coordinator`       |

---

## Quick Start

1. Create a MySQL database named `educorev3`
2. Update `application.properties` with your DB credentials
3. Run: `./mvnw spring-boot:run`
4. Open Swagger UI: [http://localhost:9098/swagger-ui/index.html](http://localhost:9098/swagger-ui/index.html)

---

## Authentication Flow

1. **Register** → `POST /api/v1/user/register`
2. **Login** → `POST /api/v1/user/login` — returns a JWT
3. In Swagger UI, click **Authorize** and paste the JWT (without "Bearer " prefix)
4. All role-specific endpoints will now work based on your role

---

## Key Endpoint Groups

### User (Public)
| Method | Path                        | Description                       |
|--------|-----------------------------|-----------------------------------|
| POST   | `/api/v1/user/register`     | Register with a role              |
| POST   | `/api/v1/user/login`        | Login and receive JWT             |
| GET    | `/api/v1/user/all`          | List all users (auth required)    |
| GET    | `/api/v1/user/{email}`      | Get user by email (auth required) |

### Student
| Method | Path                                                   | Description                          |
|--------|--------------------------------------------------------|--------------------------------------|
| PUT    | `/api/v1/student/profile/update`                       | Update own profile                   |
| GET    | `/api/v1/student/{studentId}`                          | Get student by ID                    |
| GET    | `/api/v1/student/courses/all`                          | Browse all courses                   |
| POST   | `/api/v1/student/{studentId}/course/{courseId}/enroll` | Self-enroll in a course              |
| GET    | `/api/v1/student/{studentId}/my-courses`               | View enrolled courses                |
| GET    | `/api/v1/student/{studentId}/course/{courseId}/materials` | View course materials             |
| GET    | `/api/v1/student/{studentId}/material/{fileId}/download`  | Download a material file          |
| GET    | `/api/v1/student/{studentId}/course/{courseId}/assignments` | View assignments                |
| GET    | `/api/v1/student/{studentId}/assignment/{assignmentId}/files` | View assignment files         |
| GET    | `/api/v1/student/{studentId}/assignment-file/{fileId}/download` | Download assignment file    |
| POST   | `/api/v1/student/{studentId}/assignment/{assignmentId}/submit` | Submit assignment PDF        |
| GET    | `/api/v1/student/{studentId}/my-submissions`           | View my submissions                  |
| GET    | `/api/v1/student/{studentId}/my-exams`                 | View my upcoming exams               |

### Instructor
| Method | Path                                                    | Description                           |
|--------|---------------------------------------------------------|---------------------------------------|
| PUT    | `/api/v1/instructor/profile/update`                     | Update own profile                    |
| GET    | `/api/v1/instructor/my-courses/{instructorId}`          | View assigned courses                 |
| POST   | `/api/v1/instructor/course/{courseId}/material`         | Publish course material               |
| GET    | `/api/v1/instructor/course/{courseId}/materials`        | View course materials                 |
| POST   | `/api/v1/instructor/assignment/publish`                 | Publish an assignment                 |
| GET    | `/api/v1/instructor/assignment/{assignmentId}/files`    | View assignment files                 |
| GET    | `/api/v1/instructor/course/{courseId}/submissions`      | View student submissions              |
| GET    | `/api/v1/instructor/submission/{submissionId}/download` | Download a submission                 |
| PUT    | `/api/v1/instructor/submission/{submissionId}/grade`    | Grade a submission                    |
| GET    | `/api/v1/instructor/my-exams/{instructorId}`            | View assigned exams                   |

### Registrar
| Method | Path                                                     | Description                          |
|--------|----------------------------------------------------------|--------------------------------------|
| PUT    | `/api/v1/registrar/profile/update`                       | Update own profile                   |
| POST   | `/api/v1/registrar/course`                               | Create course + assign instructor    |
| GET    | `/api/v1/registrar/courses`                              | List all courses                     |
| GET    | `/api/v1/registrar/course/{courseId}/enrolled-students`  | View enrolled students               |

### Exam Coordinator
| Method | Path                                                          | Description                     |
|--------|---------------------------------------------------------------|---------------------------------|
| POST   | `/api/v1/exam-coordinator/exams`                             | Create an exam (DRAFT)          |
| GET    | `/api/v1/exam-coordinator/exams`                             | Search/list exams               |
| GET    | `/api/v1/exam-coordinator/exams/{examId}`                    | Get exam details                |
| PUT    | `/api/v1/exam-coordinator/exams/{examId}`                    | Update exam                     |
| DELETE | `/api/v1/exam-coordinator/exams/{examId}`                    | Delete draft exam               |
| POST   | `/api/v1/exam-coordinator/exam-rooms`                        | Create room + auto-allocate     |
| GET    | `/api/v1/exam-coordinator/exam-rooms/exam/{examId}`          | Get rooms for an exam           |

---

## Project Structure

```
src/main/java/com/cts/
├── annotation/       # Custom annotations (e.g. @AuditEvent)
├── audit/            # AOP-based audit logging aspect
├── config/           # Security, JWT filter, Swagger, JPA audit config
├── controller/       # REST controllers (one per role)
├── dto/              # Request/Response DTOs
├── entity/           # JPA entities
├── enumerate/        # Enums (Role, ExamStatus)
├── exception/        # Custom exceptions + GlobalExceptionHandler
├── mapper/           # Entity ↔ DTO mappers
├── repository/       # Spring Data JPA repositories
├── service/          # Service interfaces
├── serviceimpl/      # Service implementations
└── util/             # JwtUtil, SecurityUtils
```
