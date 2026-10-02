# Fraud Detection Engine

A rule-based fraud detection system for bank money transfers. Every incoming transaction is scored by a rule engine and **approved**, **blocked** or **flagged** for manual review; fraud analysts review flagged transactions on a live dashboard and approve or reject them with a full audit trail.

![CI](https://github.com/berktopal/fraud-project/actions/workflows/ci.yml/badge.svg)

## How It Works

```
POST /api/transactions/process
        │
        ▼
 ┌──────────────────────── Rule Engine ────────────────────────┐
 │ 1. Invalid amount (≤ 0)                         → BLOCKED   │
 │ 2. Amount > 50,000 ₺                            → FLAGGED   │
 │ 3. ≥ 3 transactions from the account in 1 min  → BLOCKED   │  (velocity check)
 │ 4. Account risk score > 80 and amount > 10,000 → FLAGGED   │
 │ 5. Otherwise                                    → APPROVED  │
 └─────────────────────────────────────────────────────────────┘
        │
        ▼
 Analyst dashboard (auto-refresh every 5 s)
   FLAGGED / unreviewed BLOCKED  →  Approve ✅ / Reject ❌  →  approvedBy + processedAt recorded
```

## Features

- **Rule engine** with amount threshold, velocity (frequency) check and account risk scoring
- **Analyst review dashboard:** pending-review counter, account search, transaction detail modal (IP address, timestamps)
- **Audit trail:** reviewer and review time stored for every manual decision
- **Request validation:** positive amounts with at most 2 decimals; the client IP is taken from the connection, not from the request body
- **Unit-tested rules** (`FraudDetectionServiceTest`) and CI against PostgreSQL

## Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Data JPA, Lombok, Maven |
| Database | PostgreSQL |
| Frontend | React 19, Vite |
| Testing / CI | JUnit 5, Mockito, GitHub Actions |

## API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transactions/process` | Evaluate a transfer: `{ "accountNumber", "targetAccountNumber", "amount" }` → returns the decision |
| GET | `/api/transactions` | List all transactions |
| PUT | `/api/transactions/{id}/status?status=APPROVED\|BLOCKED` | Analyst decision |

## Project Structure

```
fraud-project/
├── fraud-engine/          # Spring Boot backend (port 8081)
│   └── src/main/java/com/berqbank/fraudengine/
│       ├── controller/    # REST endpoints, request DTO
│       ├── service/       # FraudDetectionService (rule engine)
│       ├── entity/        # Account, Transaction, TransactionStatus
│       ├── repository/    # Spring Data JPA (velocity query)
│       └── config/        # CORS
└── fraud-engine-ui/       # React analyst dashboard (Vite, port 5173)
```

## Getting Started

### Prerequisites
- JDK 21, Node.js 18+, PostgreSQL

### Backend
```bash
createdb fraud_engine_db                # or create it from your PostgreSQL client
cd fraud-engine
export DB_PASSWORD=your_postgres_password
./mvnw spring-boot:run                  # http://localhost:8081
```

Accounts are read from the `accounts` table (`account_number`, `balance`, `risk_score` 0–100); insert a few rows to start sending transactions.

### Frontend
```bash
cd fraud-engine-ui
npm install
npm run dev                             # http://localhost:5173
```

### Tests
```bash
cd fraud-engine
./mvnw verify
```
