<<<<<<< HEAD
# backend-developer-as-final-70747-prathamesh
Final Project Assignment - This repository contains the complete final project code and documentation.
=======
# RESTful Resource Booking System (`Task`)

A production-ready RESTful Resource Booking System built with **Spring Boot 3 (Java 17+)**, **Spring Security**, **JWT (JSON Web Tokens)**, **Spring Data JPA**, **Hibernate**, and **OpenAPI / Swagger UI**.

---

## Features

- **JWT Authentication & RBAC**: Stateless authentication via JWT tokens with `ROLE_ADMIN` and `ROLE_USER` permissions.
- **Resource Management**:
  - `ADMIN`: Full CRUD operations (create, update, delete, get resources).
  - `USER`: Read-only access to available bookable resources.
- **Reservation Management**:
  - `USER`: Create reservations (identity extracted automatically from JWT token context), view/update/cancel only their own reservations.
  - `ADMIN`: View, update, and manage all reservations across the system.
- **Filtering, Pagination & Sorting**:
  - Filter reservations by `status` (`PENDING`, `CONFIRMED`, `CANCELLED`), `minPrice`, and `maxPrice`.
  - Full pagination (`page`, `size`) and dynamic sorting (`sortBy`, `sortDir`).
- **Data Seeding**: Automatic pre-population of test user accounts (`admin` and `user`) and sample resources on initial startup.
- **Multi-Database Support**: Out-of-the-box in-memory H2 database, plus ready profiles for MySQL and PostgreSQL.
- **Swagger / OpenAPI Documentation**: Interactive API documentation with Bearer token authentication support.

---

## Tech Stack & Dependencies

- **Framework**: Spring Boot 3.4.3 (Java 17+)
- **Security**: Spring Security & JJWT 0.12.6
- **Persistence**: Spring Data JPA & Hibernate
- **Databases**: H2 (default in-memory), MySQL, PostgreSQL
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **API Documentation**: Springdoc OpenAPI 2.8.5 (`swagger-ui`)
- **Testing**: JUnit 5, Mockito, Spring Security Test, MockMvc

---

## Seed User Credentials

On startup, the system automatically initializes the following test users:

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` | Full access to create/update/delete resources and manage all reservations |
| `user` | `user123` | `ROLE_USER` | Can view resources, create reservations, and manage only their own reservations |

---

## Quick Start & Running the Project

### Prerequisites
- JDK 17+ (Java 25 supported)
- Maven (or use included `./mvnw.cmd` wrapper)

### Run with Default (H2 In-Memory Database)
```bash
./mvnw.cmd spring-boot:run
```
The application will start on `http://localhost:8080`.
- **H2 Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:taskdb`, User: `sa`, Password: `password`)
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### Run with MySQL Profile
Set the environment variables or pass active profile parameter:
```bash
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```
Environment Variables for MySQL (optional overrides):
- `SPRING_DATASOURCE_URL`: `jdbc:mysql://localhost:3306/taskdb`
- `SPRING_DATASOURCE_USERNAME`: `root`
- `SPRING_DATASOURCE_PASSWORD`: `yourpassword`

### Run with PostgreSQL Profile
```bash
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
```
Environment Variables for PostgreSQL (optional overrides):
- `SPRING_DATASOURCE_URL`: `jdbc:postgresql://localhost:5432/taskdb`
- `SPRING_DATASOURCE_USERNAME`: `postgres`
- `SPRING_DATASOURCE_PASSWORD`: `yourpassword`

---

## Running Tests

Execute all unit and integration tests covering authentication, RBAC, reservation ownership, filtering, and validation:

```bash
./mvnw.cmd clean test
```

---

## REST API Documentation

### 1. Authentication Endpoints

#### `POST /auth/login`
Authenticate user and obtain a JWT bearer token.
- **Request Body**:
```json
{
  "username": "user",
  "password": "user123"
}
```
- **Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "user",
  "role": "ROLE_USER"
}
```

#### `POST /auth/register`
Register a new user account.
- **Request Body**:
```json
{
  "username": "john_doe",
  "password": "password123",
  "email": "john@example.com",
  "role": "ROLE_USER"
}
```

---

### 2. Resource Management Endpoints (`/resources`)

| Method | Endpoint | Allowed Roles | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/resources` | `ROLE_USER`, `ROLE_ADMIN` | List all resources with pagination (`page`, `size`, `sortBy`, `sortDir`) |
| `GET` | `/resources/{id}` | `ROLE_USER`, `ROLE_ADMIN` | Get resource details by ID |
| `POST` | `/resources` | `ROLE_ADMIN` | Create a new bookable resource |
| `PUT` | `/resources/{id}` | `ROLE_ADMIN` | Update an existing resource |
| `DELETE` | `/resources/{id}` | `ROLE_ADMIN` | Delete a resource |

#### Create Resource Request (`POST /resources` - ADMIN)
```json
{
  "name": "Executive Boardroom",
  "description": "20-person boardroom with video conferencing setup",
  "pricePerHour": 75.00,
  "available": true
}
```

---

### 3. Reservation Management Endpoints (`/reservations`)

| Method | Endpoint | Allowed Roles | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/reservations` | `ROLE_USER`, `ROLE_ADMIN` | Create reservation (User identity automatically extracted from JWT) |
| `GET` | `/reservations` | `ROLE_USER`, `ROLE_ADMIN` | Get reservations (USER views only their own; ADMIN views all). Supports filtering by `status`, `minPrice`, `maxPrice`, `page`, `size`, `sortBy`, `sortDir` |
| `GET` | `/reservations/{id}` | `ROLE_USER`, `ROLE_ADMIN` | Get reservation by ID (USER can access only their own) |
| `PUT` | `/reservations/{id}` | `ROLE_USER`, `ROLE_ADMIN` | Update reservation status or dates |
| `DELETE` | `/reservations/{id}` | `ROLE_USER`, `ROLE_ADMIN` | Delete / Cancel reservation |

#### Create Reservation Request (`POST /reservations`)
Headers: `Authorization: Bearer <token>`
```json
{
  "resourceId": 1,
  "startTime": "2026-09-01T10:00:00",
  "endTime": "2026-09-01T12:00:00"
}
```

#### Reservation Response (`201 Created`)
```json
{
  "id": 1,
  "userId": 2,
  "username": "user",
  "resourceId": 1,
  "resourceName": "Conference Room A",
  "startTime": "2026-09-01T10:00:00",
  "endTime": "2026-09-01T12:00:00",
  "totalPrice": 100.00,
  "status": "PENDING",
  "createdAt": "2026-08-28T23:30:00",
  "updatedAt": "2026-08-28T23:30:00"
}
```

#### Filter Reservations Query Example
```http
GET /reservations?status=PENDING&minPrice=20.00&maxPrice=150.00&page=0&size=10&sortBy=totalPrice&sortDir=desc
```

---

## Project Structure

```
d:/TaskAssignment/Task/
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── task
│   │   │           ├── TaskApplication.java
│   │   │           ├── config
│   │   │           │   ├── DataInitializer.java
│   │   │           │   └── OpenApiConfig.java
│   │   │           ├── controller
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── ReservationController.java
│   │   │           │   └── ResourceController.java
│   │   │           ├── dto
│   │   │           │   ├── ErrorResponse.java
│   │   │           │   ├── JwtAuthResponse.java
│   │   │           │   ├── LoginRequest.java
│   │   │           │   ├── PagedResponse.java
│   │   │           │   ├── RegisterRequest.java
│   │   │           │   ├── ReservationRequest.java
│   │   │           │   ├── ReservationResponse.java
│   │   │           │   ├── ReservationUpdateRequest.java
│   │   │           │   ├── ResourceRequest.java
│   │   │           │   └── ResourceResponse.java
│   │   │           ├── entity
│   │   │           │   ├── Reservation.java
│   │   │           │   ├── Resource.java
│   │   │           │   └── User.java
│   │   │           ├── enums
│   │   │           │   ├── ReservationStatus.java
│   │   │           │   └── Role.java
│   │   │           ├── exception
│   │   │           │   ├── BadRequestException.java
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   ├── ResourceNotFoundException.java
│   │   │           │   └── UnauthorizedException.java
│   │   │           ├── repository
│   │   │           │   ├── ReservationRepository.java
│   │   │           │   ├── ResourceRepository.java
│   │   │           │   └── UserRepository.java
│   │   │           ├── security
│   │   │           │   ├── CustomAccessDeniedHandler.java
│   │   │           │   ├── CustomUserDetailsService.java
│   │   │           │   ├── JwtAuthenticationEntryPoint.java
│   │   │           │   ├── JwtAuthenticationFilter.java
│   │   │           │   ├── JwtTokenProvider.java
│   │   │           │   └── SecurityConfig.java
│   │   │           └── service
│   │   │               ├── AuthService.java
│   │   │               ├── ReservationService.java
│   │   │               └── ResourceService.java
│   │   └── resources
│   │       ├── application.properties
│   │       ├── application-mysql.properties
│   │       └── application-postgres.properties
│   └── test
│       └── java
│           └── com
│               └── task
│                   ├── TaskApplicationTests.java
│                   ├── controller
│                   │   ├── AuthControllerTest.java
│                   │   ├── ReservationControllerTest.java
│                   │   └── ResourceControllerTest.java
│                   └── service
│                       └── ReservationServiceTest.java
```
>>>>>>> f5cab47 (Complete RESTful Resource Booking System implementation)
