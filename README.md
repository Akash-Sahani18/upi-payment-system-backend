# UPI Payment System Backend

A **UPI-style payment backend system** built using **Spring Boot** that enables secure money transfers between users with transactional safety, JWT-based authentication, and MySQL persistence.

This project simulates real-world UPI payment workflows such as debit–credit operations, PIN verification, and transaction history management.

---

## 🚀 Live Demo

- **Live API Base URL**:[ https://<your-railway-app>.railway.app](https://upi-payment-system-backend-production.up.railway.app/)
- **Swagger UI**: [https://<your-railway-app>.railway.app/swagger-ui/index.html](https://freeshoesapi-production.up.railway.app/api/v1/docs/#/%23)

---

## 🛠 Tech Stack

- **Java 20**
- **Spring Boot**
- **Spring Security (JWT + BCrypt)**
- **Spring Data JPA (Hibernate)**
- **MySQL**
- **Swagger (OpenAPI)**

---

## ✨ Features

- Secure money transfer between UPI IDs
- JWT-based authentication for protected APIs
- BCrypt-encrypted UPI PIN verification
- Atomic debit–credit operations using `@Transactional`
- Paginated transaction history API
- Swagger UI for API testing and documentation

---

## 🧱 Architecture Overview
```
Client (Postman / Swagger / UI)
↓
Spring Boot REST APIs
↓
JWT Authentication Filter
↓
Payment Service (@Transactional)
↓
MySQL Database
```
---

## 📂 Project Structure
```bash 
src/main/java/com/upi/upi_payment_system
├── config # Security & encoder configuration
├── controller # REST controllers
├── dto # Request/response DTOs
├── model # JPA entities
├── repository # JPA repositories
├── security # JWT utilities & filters
└── service # Business logic
```
---

## 🔐 Authentication Flow

1. User logs in via `/api/auth/login`
2. Server generates a **JWT token**
3. Client sends token in `Authorization` header:

Authorization: Bearer <JWT_TOKEN>

4. JWT filter validates token for protected endpoints

---

## 🔄 API Endpoints

### Authentication
- `POST /api/auth/login` – Generate JWT token

### Payments
- `POST /api/payments/send` – Send money
- `GET /api/payments/history/{upiId}` – Get transaction history (paginated)

---

## 📥 Sample Request – Send Money
```
"senderUpi": "user1@upi",
"receiverUpi": "user2@upi",
"amount": 500,
"upiPin": "7890"

Payment Successful
```
## 🗄 Database Tables
- BankAccount
```
- id
- upiId
- balance
- upiPinHash
```
- Transaction

```
- id
- senderUpiId
- receiverUpiId
- amount
- status (SUCCESS / FAILED)
- createdAt
  ```

## ⚙️ Configuration
- application.yml location

```
src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/upi_db
    username: root
    password: password

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: your-secret-key
  expiration: 86400000
```
## 🧪 Running Locally
```
- git clone https://github.com/<your-username>/upi-payment-system.git
- cd upi-payment-system
- mvn spring-boot:run
  ```
## 🚧 Future Enhancements

- Kafka-based async transaction processing
- Role-based access control (Admin/User)
- Rate limiting & fraud detection
- Frontend UI integration


## 👨‍💻 Author
*Akash Sahani*  
📫 [GitHub](https://github.com/Akash-Sahani18) | [LinkedIn](https://www.linkedin.com/in/akash-sahani-440147243) 
