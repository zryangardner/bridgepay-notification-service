package com.bridgepay.notification.consumer

import com.bridgepay.notification.model.PaymentCreatedEvent
import com.bridgepay.notification.service.NotificationService
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SqsConsumer(private val notificationService: NotificationService) {

    private val logger = LoggerFactory.getLogger(SqsConsumer::class.java)

    @SqsListener("\${app.sqs.queue-url}")
    fun onMessage(event: PaymentCreatedEvent) {
        logger.debug("Received SQS message for payment id={}", event.id)
        notificationService.processNotification(event)
    }
}
