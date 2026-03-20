package com.bridgepay.notification.service

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.Assertions.assertEquals

@ExtendWith(MockitoExtension::class)
class SnsNotificationServiceTest {

    @Mock
    private lateinit var amazonSns: AmazonSNS

    @InjectMocks
    private lateinit var snsNotificationService: SnsNotificationService

    @Test
    fun `sendSms publishes a request with the correct phone number and message`() {
        val phoneNumber = "+15551234567"
        val message = "BridgePay: Payment of 100.0 USD is PENDING."

        `when`(amazonSns.publish(org.mockito.ArgumentMatchers.any(PublishRequest::class.java)))
            .thenReturn(PublishResult().withMessageId("msg-id-001"))

        snsNotificationService.sendSms(phoneNumber, message)

        val captor = ArgumentCaptor.forClass(PublishRequest::class.java)
        verify(amazonSns).publish(captor.capture())

        assertEquals(phoneNumber, captor.value.phoneNumber)
        assertEquals(message, captor.value.message)
    }
}
