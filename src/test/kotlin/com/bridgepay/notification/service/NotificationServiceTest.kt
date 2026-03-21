package com.bridgepay.notification.service

import com.bridgepay.notification.model.PaymentResultEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class NotificationServiceTest {

    @Mock
    private lateinit var snsNotificationService: SnsNotificationService

    @Mock
    private lateinit var emailNotificationService: EmailNotificationService

    private val completedEvent = PaymentResultEvent(
        paymentId = "payment-123",
        amount = 100.0,
        currency = "USD",
        status = "COMPLETED",
        senderId = "user-001",
        recipientId = "user-002"
    )

    private val failedEvent = PaymentResultEvent(
        paymentId = "payment-123",
        amount = 100.0,
        currency = "USD",
        status = "FAILED",
        senderId = "user-001",
        recipientId = "user-002",
        reason = "Insufficient funds"
    )

    @Test
    fun `COMPLETED - both sender and recipient receive SMS and email`() {
        val service = NotificationService(
            notificationsEnabled = true,
            snsNotificationService = Optional.of(snsNotificationService),
            emailNotificationService = Optional.of(emailNotificationService)
        )

        service.processNotification(completedEvent)

        val expectedSms = "BridgePay: Payment of 100.0 USD completed successfully."
        verify(snsNotificationService).sendSms("user-001", expectedSms)
        verify(snsNotificationService).sendSms("user-002", expectedSms)

        verify(emailNotificationService).sendEmail(
            "user-001", "BridgePay Payment Completed",
            "Your payment was sent successfully.\n\nAmount:    100.0 USD\nStatus:    COMPLETED\nReference: payment-123"
        )
        verify(emailNotificationService).sendEmail(
            "user-002", "BridgePay Payment Received",
            "You received a payment.\n\nAmount:    100.0 USD\nStatus:    COMPLETED\nReference: payment-123"
        )
    }

    @Test
    fun `FAILED - only sender receives SMS and email`() {
        val service = NotificationService(
            notificationsEnabled = true,
            snsNotificationService = Optional.of(snsNotificationService),
            emailNotificationService = Optional.of(emailNotificationService)
        )

        service.processNotification(failedEvent)

        val expectedSms = "BridgePay: Payment of 100.0 USD failed. Insufficient funds"
        verify(snsNotificationService).sendSms("user-001", expectedSms)
        // only one SMS total — recipient is never notified on failure
        verify(snsNotificationService, org.mockito.Mockito.times(1))
            .sendSms(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())

        verify(emailNotificationService).sendEmail(
            "user-001", "BridgePay Payment Failed",
            "Your payment could not be processed. Insufficient funds\n\nAmount:    100.0 USD\nStatus:    FAILED\nReference: payment-123"
        )
        // only one email total — recipient is never notified on failure
        verify(emailNotificationService, org.mockito.Mockito.times(1))
            .sendEmail(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    fun `when notifications disabled, neither sendSms nor sendEmail are called`() {
        val service = NotificationService(
            notificationsEnabled = false,
            snsNotificationService = Optional.of(snsNotificationService),
            emailNotificationService = Optional.of(emailNotificationService)
        )

        service.processNotification(completedEvent)

        verify(snsNotificationService, never()).sendSms(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())
        verify(emailNotificationService, never()).sendEmail(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())
    }
}
