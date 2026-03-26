# FinFlow Loan Management System

## Project Structure
```
finflow/
├── auth-service/        PORT: 8081  - Register, Login, JWT
├── application-service/ PORT: 8082  - Loan application lifecycle
├── document-service/    PORT: 8083  - Document upload & verify
├── admin-service/       PORT: 8084  - Decisions, reports
└── api-gateway/         PORT: 8080  - Single entry point (start this LAST)
```

## Setup Steps

### 1. PostgreSQL mein 4 databases banao
```sql
CREATE DATABASE auth_db;
CREATE DATABASE loan_db;
CREATE DATABASE doc_db;
CREATE DATABASE admin_db;
```

### 2. Har service ki application.properties mein password daalo
`YOUR_POSTGRES_PASSWORD` ki jagah apna actual PostgreSQL password daalo

### 3. Services is order mein start karo
1. auth-service
2. application-service
3. document-service
4. admin-service
5. api-gateway  <-- LAST mein

### 4. Test karo Postman se
```
POST http://localhost:8080/gateway/auth/signup
POST http://localhost:8080/gateway/auth/login
```

## Packages (har service mein same structure)
- `controller/`   - HTTP requests handle karta hai
- `service/`      - Business logic yahan hoti hai
- `repository/`   - Database se baat karta hai
- `entity/`       - Database tables (JPA entities)
- `dto/`          - Data Transfer Objects (request/response)
- `config/`       - Configuration classes
- `security/`     - JWT filter, SecurityConfig (sirf auth-service mein)
