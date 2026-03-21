package com.bridgepay.notification.service

import com.bridgepay.notification.model.PaymentResultEvent
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

    fun processNotification(event: PaymentResultEvent) {
        logger.info(
            "Processing notification [paymentId={}, status={}, sender={}, recipient={}]",
            event.paymentId, event.status, event.senderId, event.recipientId
        )

        if (!notificationsEnabled) {
            logger.info(
                "Notifications suppressed — would notify senderId={}, recipientId={}",
                event.senderId,
                if (event.status == "COMPLETED") event.recipientId else "n/a"
            )
            return
        }

        when (event.status) {
            "COMPLETED" -> {
                val msg = "BridgePay: Payment of ${event.amount} ${event.currency} completed successfully."
                snsNotificationService.ifPresent { it.sendSms(event.senderId, msg) }
                snsNotificationService.ifPresent { it.sendSms(event.recipientId, msg) }
                emailNotificationService.ifPresent {
                    it.sendEmail(event.senderId, "BridgePay Payment Completed",
                        buildEmailBody(event, "Your payment was sent successfully."))
                    it.sendEmail(event.recipientId, "BridgePay Payment Received",
                        buildEmailBody(event, "You received a payment."))
                }
            }
            "FAILED" -> {
                val msg = "BridgePay: Payment of ${event.amount} ${event.currency} failed. ${event.reason ?: ""}".trim()
                snsNotificationService.ifPresent { it.sendSms(event.senderId, msg) }
                emailNotificationService.ifPresent {
                    it.sendEmail(event.senderId, "BridgePay Payment Failed",
                        buildEmailBody(event, "Your payment could not be processed. ${event.reason ?: ""}".trim()))
                }
            }
            else -> logger.warn("Unknown payment status '{}' for paymentId={}", event.status, event.paymentId)
        }
    }

    private fun buildEmailBody(event: PaymentResultEvent, headline: String) = """
        $headline

        Amount:    ${event.amount} ${event.currency}
        Status:    ${event.status}
        Reference: ${event.paymentId}
    """.trimIndent()
}
