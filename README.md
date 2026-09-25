# UPI Payment System Backend

A UPI-style payment backend built with Java and Spring Boot. The application supports authentication, secure money transfers, transaction history, PIN verification, and safeguards for concurrent payment requests.

## Tech Stack

- Java 17
- Spring Boot 3.3.6
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- MySQL
- H2
- Maven
- OpenAPI / Swagger

## Features

- JWT authentication for protected APIs
- BCrypt-based UPI PIN verification
- Secure money transfers between UPI accounts
- Atomic debit and credit operations using `@Transactional`
- Pessimistic row-level locking for concurrent transfers
- Idempotency keys to prevent duplicate transactions
- Paginated transaction history
- Global exception handling
- OpenAPI / Swagger API documentation
- Integration testing with an H2 in-memory database

## API

### Authentication

#### Login

```http

POST /api/auth/login

```

Used to authenticate a user and obtain a JWT token.

### Payments

#### Send Money

```http

POST /api/payments/send

```

Required request data:

```json

{

"senderUpi": "sender@upi",

"receiverUpi": "receiver@upi",

"amount": 500.00,

"upiPin": "1234"

}

```

Required request header:

```http

Authorization: Bearer <JWT>

Idempotency-Key: <unique-key>

```

The `Idempotency-Key` uniquely identifies a payment request and prevents the same payment from being processed more than once.

#### Transaction History

```http

GET /api/payments/history/{upiId}

```

Returns the transaction history for the specified UPI ID with pagination support.

## Payment Processing

The payment flow is handled inside a transactional service boundary.

```text

Client

|

v

Payment Controller

|

v

Payment Service

|

+--> Check Idempotency Key

|

v

Payment Transaction Service

|

+--> Lock Sender Account

|

+--> Lock Receiver Account

|

+--> Verify UPI PIN

|

+--> Check Balance

|

+--> Debit Sender

|

+--> Credit Receiver

|

+--> Save Transaction

|

v

Response

```

If an error occurs during the transfer, the database transaction is rolled back to prevent partial debit or credit operations.

## Concurrency and Idempotency

The payment system uses pessimistic row-level locking when loading bank accounts for a transfer. This prevents concurrent transactions from modifying the same account balance at the same time.

Each transaction also stores a unique `idempotencyKey`.

If the same idempotency key is submitted multiple times, including concurrently, the system returns the existing transaction instead of creating another payment.

### Concurrent Request Test

The integration test submits:

- 200 total requests
- 100 unique idempotency keys
- Each payment submitted twice
- 20 concurrent worker threads

The test verifies:

- All requests receive successful responses
- Only 100 transactions are created
- Duplicate payment processing is prevented
- Final sender and receiver balances are correct

## Database

The application uses MySQL for persistent data storage.

The main entities are:

```text

BankAccount

|

+-- UPI ID

+-- Account Number

+-- Bank Name

+-- Balance

+-- UPI PIN Hash

Transaction

|

+-- Sender UPI ID

+-- Receiver UPI ID

+-- Amount

+-- Status

+-- Idempotency Key

+-- Created At

```

The `transactions` table requires a unique `idempotency_key` column for the current payment flow.

```sql

ALTER TABLE transactions

ADD COLUMN idempotency_key VARCHAR(100) NOT NULL UNIQUE;

```

For an existing database containing transaction records, the migration should be performed in stages so existing records can be populated before applying the `NOT NULL` and `UNIQUE` constraints.

## Project Structure

```text

src
├── main
│   ├── java
│   │   ├── com/upi/upi_payment_system
│   │   │   ├── config
│   │   │   ├── controller
│   │   │   ├── exception
│   │   │   ├── model
│   │   │   ├── repository
│   │   │   ├── security
│   │   │   └── service
│   │   └── dto
│   │
│   └── resources
│       └── application.yaml
│
└── test
    ├── java
    └── resources
        └── application.yaml
```

## Configuration

Configure the database and JWT settings in:

```text

src/main/resources/application.yaml

```

Sensitive values should be supplied through environment variables rather than committed directly to the repository.

Example:

```yaml

spring:

datasource:

url: jdbc:mysql://localhost:3306/upi_payment_system

username: ${DB_USERNAME}

password: ${DB_PASSWORD}

jwt:

secret: ${JWT_SECRET}

```

Do not commit database passwords, JWT secrets, API keys, or other credentials to the repository.

## Run Locally

### 1. Clone the repository

```bash

git clone https://github.com/Akash-Sahani18/upi-payment-system-backend.git

cd upi-payment-system-backend

```

### 2. Configure the database

Create a MySQL database and configure the required database and JWT environment variables.

### 3. Run the tests

Linux / macOS:

```bash

./mvnw clean test

```

Windows:

```cmd

mvnw.cmd clean test

```

### 4. Start the application

Linux / macOS:

```bash

./mvnw spring-boot:run

```

Windows:

```cmd

mvnw.cmd spring-boot:run

```

The application runs on the configured Spring Boot port.

## API Documentation

The project uses OpenAPI / Swagger for API documentation.

When the application is running, Swagger UI is available at:

```text

http://localhost:8080/swagger-ui/index.html

```

## Testing

The project includes integration testing for the payment flow using an H2 in-memory database.

The concurrency test verifies that simultaneous payment requests maintain correct balances and that duplicate idempotency keys do not create duplicate transactions.

Run all tests with:

```bash

mvn clean test

```

## Security

The backend implements:

- JWT-based authentication
- Spring Security
- BCrypt UPI PIN hashing and verification
- Protected payment endpoints
- User authorization
- Database-level idempotency constraints
- Environment-based configuration for sensitive credentials

## License

This project is intended for learning and software engineering portfolio purposes.
