package com.bridgepay.notification.service

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["notifications.enabled"], havingValue = "true")
class SnsNotificationService(private val amazonSns: AmazonSNS) {

    private val logger = LoggerFactory.getLogger(SnsNotificationService::class.java)

    fun sendSms(phoneNumber: String, message: String) {
        val request = PublishRequest()
            .withPhoneNumber(phoneNumber)
            .withMessage(message)

        val result = amazonSns.publish(request)
        logger.info("SMS sent to {} — messageId={}", phoneNumber, result.messageId)
    }
}
