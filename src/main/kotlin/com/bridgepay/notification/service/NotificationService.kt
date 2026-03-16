package com.bridgepay.notification.service

import com.bridgepay.notification.model.PaymentCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationService {

    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    fun processNotification(event: PaymentCreatedEvent) {
        logger.info(
            "Processing notification for payment [id={}, amount={} {}, status={}, sender={}, recipient={}]",
            event.id,
            event.amount,
            event.currency,
            event.status,
            event.senderId,
            event.recipientId
        )

        // TODO: dispatch SMS notification to recipient
        // TODO: dispatch email notification to sender
    }
}
