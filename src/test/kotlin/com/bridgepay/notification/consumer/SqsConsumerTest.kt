package com.bridgepay.notification.consumer

import com.bridgepay.notification.model.PaymentResultEvent
import com.bridgepay.notification.service.NotificationService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SqsConsumerTest {

    @Mock
    private lateinit var notificationService: NotificationService

    @InjectMocks
    private lateinit var sqsConsumer: SqsConsumer

    private val event = PaymentResultEvent(
        paymentId = "payment-456",
        amount = 250.0,
        currency = "USD",
        status = "COMPLETED",
        senderId = "user-003",
        recipientId = "user-004"
    )

    @Test
    fun `onPaymentResult delegates to notificationService processNotification with the correct event`() {
        sqsConsumer.onPaymentResult(event)

        verify(notificationService).processNotification(event)
    }

    @Test
    fun `onDeadLetter logs the event and does not delegate to notificationService`() {
        sqsConsumer.onDeadLetter(event)

        verify(notificationService, never()).processNotification(event)
    }
}
