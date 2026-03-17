# BridgePay Notification Service

> SQS-driven payment lifecycle notification dispatcher built with Kotlin and Spring Boot.

A microservice that consumes payment events from AWS SQS and dispatches lifecycle notifications to payment participants. Part of the BridgePay suite — a portfolio project demonstrating polyglot, event-driven microservices architecture on AWS.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Framework | Spring Boot 3.5.11 |
| Messaging | AWS SQS (Spring Cloud AWS 3.3.0) |
| Build | Maven |
| Testing | JUnit 5 |

---

## Architecture

```
AWS SQS Queue (bridgepay-payment-events)
     │
     ▼
SqsConsumer                (Async message listener)
     │
     ▼
NotificationService        (Notification dispatch logic)
     │
     ├──▶ SMS (AWS SNS)     (coming soon)
     └──▶ Email (SendGrid)  (coming soon)
```

---

## Project Structure

```
src/main/kotlin/com/bridgepay/notification/
├── consumer/          # SqsConsumer — @SqsListener entry point
├── service/           # NotificationService — dispatch logic
└── model/             # PaymentCreatedEvent — SQS message contract

src/main/resources/
└── application.properties    # AWS region, SQS queue URL
```

---

## How It Works

The payment processor publishes a `PaymentCreatedEvent` to the `bridgepay-payment-events` SQS queue whenever a new payment is created. This service listens on that queue, deserializes the event, and routes a notification to the payment sender and recipient based on the payment status.

### PaymentCreatedEvent Contract

```json
{
  "id": "f1f08280-bd82-4951-8076-8ae7969c4b95",
  "amount": 100.00,
  "currency": "USD",
  "status": "PENDING",
  "senderId": "user-001",
  "recipientId": "user-002"
}
```

---

## Configuration

| Property | Description |
|---|---|
| `spring.cloud.aws.region.static` | AWS region |
| `app.sqs.queue-url` | Full SQS queue URL |

### Running Locally

```bash
./mvnw spring-boot:run
```

Set the following environment variables for AWS credentials:

| Variable | Description |
|---|---|
| `AWS_ACCESS_KEY_ID` | AWS credentials |
| `AWS_SECRET_ACCESS_KEY` | AWS credentials |

---

## Roadmap

### Completed

- [x] SqsConsumer listening on `bridgepay-payment-events` queue
- [x] PaymentCreatedEvent deserialization via Jackson Kotlin module
- [x] NotificationService with structured logging

### Planned

- [ ] AWS SNS integration for SMS notifications
- [ ] SendGrid integration for email notifications
- [ ] Dead letter queue (DLQ) for failed notification events
- [ ] Dockerize and deploy to AWS ECS Fargate

---

## Related Projects

| Repo | Stack | Description |
|---|---|---|
| `bridgepay-payment-processor` | Java 21 / Spring Boot / AWS | Core payment processing API |
| `bridgepay-insights-api` | Python / FastAPI | AI-powered payment insights API *(coming soon)* |
| `bridgepay-dashboard` | React | Payment status dashboard and registration UI *(coming soon)* |

---

## Author

Zachary Gardner — [LinkedIn](https://linkedin.com/in/zryangardner) · [GitHub](https://github.com/zryangardner)
