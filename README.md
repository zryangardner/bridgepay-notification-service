# BridgePay Notification Service

> SQS-driven payment lifecycle notification dispatcher built with Kotlin and Spring Boot.

A microservice that consumes payment events from AWS SQS and dispatches lifecycle notifications to payment participants via SMS (AWS SNS) and email (SendGrid). Part of the BridgePay suite — a portfolio project demonstrating polyglot, event-driven microservices architecture on AWS.

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
bridgepay-payment-processor
     │
     │  publishes PaymentCreatedEvent
     ▼
AWS SQS Queue (bridgepay-payment-events)
     │
     ▼
SqsConsumer                  (@SqsListener — async message entry point)
     │
     ▼
NotificationService           (Dispatch logic — feature-toggled)
     │
     ├──▶ SnsNotificationService    (SMS via AWS SNS)
     └──▶ EmailNotificationService  (Email via SendGrid)

AWS SQS DLQ (bridgepay-payment-events-dlq)
     │
     ▼
SqsConsumer.onDeadLetter()    (ERROR logging — failed message handling)
```

---

## How It Works

The payment processor publishes a `PaymentCreatedEvent` to the `bridgepay-payment-events` SQS queue whenever a new payment is created. This service listens on that queue, deserializes the event, and routes notifications to the payment sender and recipient.

If message processing fails repeatedly, SQS routes the message to the dead letter queue (`bridgepay-payment-events-dlq`) where it is logged at ERROR level for observability and recovery.

Notification dispatch is feature-toggled via `notifications.enabled` — when disabled, the service logs what would have been sent without making external calls. This allows the full integration to be verified in code without requiring live SNS/SendGrid credentials in every environment.

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

| Property | Environment Variable | Description |
|---|---|---|
| `spring.cloud.aws.region.static` | `AWS_REGION` | AWS region |
| `app.sqs.queue-url` | `SQS_QUEUE_URL` | Full SQS queue URL |
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
| `SQS_QUEUE_URL` | Full SQS queue URL |
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
  -e SQS_QUEUE_URL=<queue-url> \
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
| `NotificationServiceTest` | Unit — Mockito | Toggle on: verifies SNS + email called with correct args; toggle off: verifies neither is called |
| `SqsConsumerTest` | Unit — Mockito | Verifies consumer delegates to NotificationService on message and dead letter receipt |
| `SnsNotificationServiceTest` | Unit — Mockito | Verifies PublishRequest constructed correctly and AmazonSNS.publish() called |

6 tests passing.

---

## Project Structure

```
src/main/kotlin/com/bridgepay/notification/
├── config/            # NotificationConfig — conditional AmazonSNS and SendGrid beans
├── consumer/          # SqsConsumer — @SqsListener for main queue and DLQ
├── service/           # NotificationService, SnsNotificationService, EmailNotificationService
└── model/             # PaymentCreatedEvent — SQS message contract

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
- [x] SqsConsumer listening on `bridgepay-payment-events` queue
- [x] PaymentCreatedEvent deserialization via Jackson Kotlin module
- [x] AWS SNS integration for SMS notifications (feature-toggled)
- [x] SendGrid integration for email notifications (feature-toggled)
- [x] Dead letter queue (DLQ) listener with ERROR logging
- [x] Dockerized application with multi-stage build
- [x] Unit tests — 6 passing across consumer, service, and SNS layers

### Planned
- [ ] GitHub Actions CI/CD pipeline — build, test, push to ECR
- [ ] Deploy to AWS ECS Fargate (via Terraform)
- [ ] User lookup integration — resolve senderId/recipientId to real contact info via bridgepay-registration-service

---

## Related Projects

| Repo | Stack | Description |
|---|---|---|
| `bridgepay-payment-processor` | Java 21 / Spring Boot / AWS SQS | Core payment processing API — publishes events this service consumes |
| `bridgepay-registration-service` | TypeScript / Node.js / Express | User registration and JWT auth — future source of contact info for notifications |
| `bridgepay-dashboard` | React | Frontend — payment status, transaction history, onboarding |
| `bridgepay-terraform` | Terraform | AWS infrastructure — provisions all services |

---

## Author

Zachary Gardner — [LinkedIn](https://linkedin.com/in/zryangardner) · [GitHub](https://github.com/zryangardner)