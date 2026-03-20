package com.bridgepay.notification.service

import com.bridgepay.notification.model.PaymentCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class NotificationService(
    @Value("\${notifications.enabled}") private val notificationsEnabled: Boolean,
    private val snsNotificationService: Optional<SnsNotificationService>,
    private val emailNotificationService: Optional<EmailNotificationService>
) {

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

        if (!notificationsEnabled) {
            logger.info(
                "Notification suppressed (notifications.enabled=false) — would have notified senderId: {}, recipientId: {}",
                event.senderId,
                event.recipientId
            )
            return
        }

        val smsBody = "BridgePay: Payment of ${event.amount} ${event.currency} is ${event.status}."
        snsNotificationService.ifPresent { it.sendSms(event.recipientId, smsBody) }

        val emailSubject = "BridgePay Payment ${event.status}"
        val emailBody = """
            Your BridgePay payment has been updated.

            Amount:    ${event.amount} ${event.currency}
            Status:    ${event.status}
            Reference: ${event.id}
        """.trimIndent()
        emailNotificationService.ifPresent { it.sendEmail(event.senderId, emailSubject, emailBody) }
    }
}
