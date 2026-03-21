package com.bridgepay.notification.consumer

import com.bridgepay.notification.model.PaymentResultEvent
import com.bridgepay.notification.service.NotificationService
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SqsConsumer(private val notificationService: NotificationService) {
    private val logger = LoggerFactory.getLogger(SqsConsumer::class.java)

    @SqsListener("\${app.sqs.payment-results-queue-url}")
    fun onPaymentResult(event: PaymentResultEvent) {
        logger.debug("Received payment result for paymentId={} status={}", event.paymentId, event.status)
        notificationService.processNotification(event)
    }

    @SqsListener("\${app.sqs.dlq-url}")
    fun onDeadLetter(event: PaymentResultEvent) {
        logger.error(
            "Dead letter received [paymentId={}, amount={} {}, status={}, sender={}, recipient={}, reason={}]",
            event.paymentId, event.amount, event.currency,
            event.status, event.senderId, event.recipientId, event.reason
        )
    }
}
