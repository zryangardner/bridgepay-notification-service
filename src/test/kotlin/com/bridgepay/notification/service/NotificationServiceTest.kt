package com.bridgepay.notification.service

import com.bridgepay.notification.model.PaymentCreatedEvent
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

    private val event = PaymentCreatedEvent(
        id = "payment-123",
        amount = 100.0,
        currency = "USD",
        status = "PENDING",
        senderId = "user-001",
        recipientId = "user-002"
    )

    @Test
    fun `when notifications enabled, sendSms and sendEmail are called with correct arguments`() {
        val service = NotificationService(
            notificationsEnabled = true,
            snsNotificationService = Optional.of(snsNotificationService),
            emailNotificationService = Optional.of(emailNotificationService)
        )

        service.processNotification(event)

        val expectedSmsBody = "BridgePay: Payment of 100.0 USD is PENDING."
        verify(snsNotificationService).sendSms("user-002", expectedSmsBody)

        val expectedEmailSubject = "BridgePay Payment PENDING"
        val expectedEmailBody = """
            Your BridgePay payment has been updated.

            Amount:    100.0 USD
            Status:    PENDING
            Reference: payment-123
        """.trimIndent()
        verify(emailNotificationService).sendEmail("user-001", expectedEmailSubject, expectedEmailBody)
    }

    @Test
    fun `when notifications disabled, neither sendSms nor sendEmail are called`() {
        val service = NotificationService(
            notificationsEnabled = false,
            snsNotificationService = Optional.of(snsNotificationService),
            emailNotificationService = Optional.of(emailNotificationService)
        )

        service.processNotification(event)

        verify(snsNotificationService, never()).sendSms(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())
        verify(emailNotificationService, never()).sendEmail(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())
    }
}
