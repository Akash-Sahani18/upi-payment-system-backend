# 💳 UPI Payment System – Secure Backend API
UPI payment system backend built with Spring Boot, JWT authentication, and MySQL, supporting secure transactions, PIN validation, and transaction history APIs.

## 📌 Features

🔐 JWT-based Authentication & Authorization
💸 UPI PIN validation using BCrypt
🏦 Atomic money transfer (Debit + Credit)
📜 Transaction history with pagination
🧾 Failure & success transaction tracking
⚡ Stateless REST APIs
🧩 Clean layered architecture (Controller / Service / Repository)

## 📌 Features

Backend: Java 20, Spring Boot 3.x
Security: Spring Security, JWT, BCrypt
Database: MySQL, Spring Data JPA, Hibernate
API Docs: Swagger (Springdoc OpenAPI)
Build Tool: Maven

## 📂 Project Structure
upi-payment-system/
├── src/main/java/com/upi/upi_payment_system
│   ├── config/        # Security & JWT config
│   ├── controller/    # REST Controllers
│   ├── dto/           # Request / Response DTOs
│   ├── model/         # JPA Entities
│   ├── repository/    # Spring Data JPA Repositories
│   ├── security/      # JWT Filter & Utils
│   └── service/       # Business Logic
│
├── src/main/resources
│   └── application.yml
│
├── pom.xml
└── README.md


## 🔐 Authentication Flow (JWT)

-User logs in → receives JWT token
-Client sends token in header:
makefile{
  
}
- JwtAuthenticationFilter validates token
- Request proceeds to secured endpoints


## 👨‍💻 Author
*Akash Sahani*  
📫 [GitHub](https://github.com/Akash-Sahani18) | [LinkedIn](https://www.linkedin.com/in/akash-sahani-440147243)





