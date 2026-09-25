# UPI Payment System Backend

A UPI-style payment backend built with Spring Boot. It supports authentication, money transfers, transaction history, PIN verification, and transaction safety.

## Tech Stack

- Java 17
- Spring Boot 3.3.6
- Spring Security and JWT
- Spring Data JPA / Hibernate
- MySQL
- OpenAPI / Swagger

## Features

- JWT authentication for protected APIs
- BCrypt-based UPI PIN verification
- Atomic debit and credit operations with `@Transactional`
- Row-level locking for concurrent transfers
- Idempotency keys to prevent duplicate transactions
- Paginated transaction history
- Swagger API documentation

## API

### Authentication

`POST /api/auth/login`

### Payments

`POST /api/payments/send`

Required request parameters:

- `senderUpi`
- `receiverUpi`
- `amount`
- `upiPin`
- `Idempotency-Key` request header

`GET /api/payments/history/{upiId}`

## Concurrency and Idempotency

The payment flow locks the sender and receiver accounts while the transfer is processed. Each transaction also stores a unique idempotency key. If the same key is submitted concurrently, only one transaction is created and the other request returns the existing result.

The test suite submits 200 concurrent requests using 100 unique idempotency keys and verifies the final account balances and transaction count.

## Project Structure

```text
src/main/java/com/upi/upi_payment_system
├── config
├── controller
├── dto
├── model
├── repository
├── security
└── service
```

## Run Locally

Configure the database and JWT settings in `src/main/resources/application.yaml`, then run:

```bash
mvn clean test
mvn spring-boot:run
```

## Database

The `transactions` table requires a unique `idempotency_key` column for the current payment flow.

```sql
ALTER TABLE transactions
    ADD COLUMN idempotency_key VARCHAR(100) NOT NULL UNIQUE;
```
