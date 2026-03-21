# BridgePay Notification Service

> SQS-driven payment lifecycle notification dispatcher built with Kotlin and Spring Boot.

A microservice that consumes payment result events from AWS SQS and dispatches lifecycle notifications to payment participants via SMS (AWS SNS) and email (SendGrid). Part of the BridgePay suite — a portfolio project demonstrating polyglot, event-driven microservices architecture on AWS.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Framework | Spring Boot 3.5.11 |
| Messaging | AWS SQS (Spring Cloud AWS 3.3.0) |
| SMS | AWS SNS |
| Email | SendGrid |
| Cloud | AWS — ECS Fargate, SQS, ECR |
| Containerization | Docker |
| Build | Maven |
| Testing | JUnit 5, Mockito |

---

## Architecture
```
bridgepay-payment-service
     │
     │  publishes PaymentProcessedEvent / PaymentFailedEvent
     ▼
AWS SQS Queue (bridgepay-payment-results)
     │
     ▼
SqsConsumer                  (@SqsListener — async message entry point)
     │
     ▼
NotificationService           (Dispatch logic — status-aware routing)
     │
     ├──▶ SnsNotificationService    (SMS via AWS SNS)
     └──▶ EmailNotificationService  (Email via SendGrid)

AWS SQS DLQ (bridgepay-payment-results-dlq)
     │
     ▼
SqsConsumer.onDeadLetter()    (ERROR logging — failed message handling)
```

---

## How It Works

When a payment completes or fails, `bridgepay-payment-service` publishes a `PaymentResultEvent` to the `bridgepay-payment-results` SQS queue. This service listens on that queue, deserializes the event, and routes notifications based on payment status:

- **COMPLETED** — sender and recipient are both notified via SMS and email
- **FAILED** — sender only is notified with the failure reason

If message processing fails repeatedly, SQS routes the message to the dead letter queue where it is logged at ERROR level for observability and recovery.

Notification dispatch is feature-toggled via `notifications.enabled` — when disabled, the service logs what would have been sent without making external calls. This allows the full integration to be verified in any environment without requiring live SNS/SendGrid credentials.

### PaymentResultEvent Contract
```json
{
  "paymentId": "f1f08280-bd82-4951-8076-8ae7969c4b95",
  "senderId": "user-001",
  "recipientId": "user-002",
  "amount": 100.00,
  "currency": "USD",
  "status": "COMPLETED",
  "reason": null
}
```

`reason` is populated on `FAILED` events with a human-readable failure description.

---

## Configuration

| Property | Environment Variable | Description |
|---|---|---|
| `spring.cloud.aws.region.static` | — | AWS region (static: us-east-1) |
| `app.sqs.payment-results-queue-url` | `SQS_PAYMENT_RESULTS_QUEUE_URL` | Full SQS payment results queue URL |
| `app.sqs.dlq-url` | `SQS_DLQ_URL` | Full SQS dead letter queue URL |
| `notifications.enabled` | `NOTIFICATIONS_ENABLED` | Enable live SNS/SendGrid dispatch (default: false) |
| `aws.sns.region` | `AWS_REGION` | SNS region for SMS dispatch |
| `sendgrid.api-key` | `SENDGRID_API_KEY` | SendGrid API key for email dispatch |

---

## Running Locally

### Prerequisites

- Java 21
- Maven

### Start the application
```bash
./mvnw spring-boot:run
```

Set the following environment variables:

| Variable | Description |
|---|---|
| `AWS_ACCESS_KEY_ID` | AWS credentials |
| `AWS_SECRET_ACCESS_KEY` | AWS credentials |
| `SQS_PAYMENT_RESULTS_QUEUE_URL` | Full SQS payment results queue URL |
| `SQS_DLQ_URL` | Full SQS dead letter queue URL |
| `NOTIFICATIONS_ENABLED` | `false` for local dev — suppresses live SNS/SendGrid calls |

With `NOTIFICATIONS_ENABLED=false`, the service consumes and processes SQS events normally but logs notification output instead of dispatching live SMS or email.

---

## Docker

### Build the image
```bash
docker build -t bridgepay-notification-service .
```

### Run the container
```bash
docker run \
  -e AWS_ACCESS_KEY_ID=<key> \
  -e AWS_SECRET_ACCESS_KEY=<secret> \
  -e SQS_PAYMENT_RESULTS_QUEUE_URL=<queue-url> \
  -e SQS_DLQ_URL=<dlq-url> \
  -e NOTIFICATIONS_ENABLED=false \
  bridgepay-notification-service
```

---

## Running Tests
```bash
./mvnw test
```

### Test Coverage

| Class | Type | What It Covers |
|---|---|---|
| `NotificationServiceTest` | Unit — Mockito | COMPLETED routing (sender + recipient notified), FAILED routing (sender only), notifications disabled |
| `SqsConsumerTest` | Unit — Mockito | Delegates to NotificationService on message receipt and dead letter |
| `SnsNotificationServiceTest` | Unit — Mockito | Verifies PublishRequest constructed correctly and AmazonSNS.publish() called |

7 tests passing.

---

## Project Structure
```
src/main/kotlin/com/bridgepay/notification/
├── config/            # NotificationConfig — conditional AmazonSNS and SendGrid beans
├── consumer/          # SqsConsumer — @SqsListener for results queue and DLQ
├── service/           # NotificationService, SnsNotificationService, EmailNotificationService
└── model/             # PaymentResultEvent — SQS message contract

src/main/resources/
└── application.properties    # AWS region, SQS URLs, notification toggle

src/test/
├── kotlin/            # NotificationServiceTest, SqsConsumerTest, SnsNotificationServiceTest
└── resources/
    └── application.properties    # Test config — notifications.enabled=false, mocked AWS
```

---

## Roadmap

### Completed
- [x] SqsConsumer listening on `bridgepay-payment-results` queue
- [x] PaymentResultEvent deserialization via Jackson Kotlin module
- [x] Status-aware notification routing — COMPLETED notifies sender + recipient, FAILED notifies sender only
- [x] AWS SNS integration for SMS notifications (feature-toggled)
- [x] SendGrid integration for email notifications (feature-toggled)
- [x] Dead letter queue (DLQ) listener with ERROR logging
- [x] Dockerized application with multi-stage build
- [x] Unit tests — 7 passing across consumer, service, and SNS layers

### Planned
- [ ] GitHub Actions CI/CD pipeline — build, test, push to ECR
- [ ] Deploy to AWS ECS Fargate (via Terraform)
- [ ] User lookup integration — resolve senderId/recipientId to real contact info via bridgepay-account-service

---

## Related Projects

| Repo | Stack | Description |
|---|---|---|
| `bridgepay-payment-service` | Java 21 / Spring Boot / AWS SQS | Core payment processing API — publishes result events this service consumes |
| `bridgepay-account-service` | TypeScript / Node.js / Express | User accounts, social graph, and payment processing |
| `bridgepay-dashboard` | React / Vite / TypeScript | Frontend — payment status, transaction history, friends |
| `bridgepay-terraform` | Terraform | AWS infrastructure — provisions all services |

---

## Author

Zachary Gardner — [LinkedIn](https://linkedin.com/in/zryangardner) · [GitHub](https://github.com/zryangardner)