package com.bridgepay.notification.service

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["notifications.enabled"], havingValue = "true")
class EmailNotificationService(private val sendGrid: SendGrid) {

    private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)

    fun sendEmail(recipientEmail: String, subject: String, body: String) {
        val mail = Mail(
            Email("noreply@bridgepay.com"),
            subject,
            Email(recipientEmail),
            Content("text/plain", body)
        )

        val request = Request().apply {
            method = Method.POST
            endpoint = "mail/send"
            this.body = mail.build()
        }

        val response = sendGrid.api(request)
        logger.info("Email sent to {} — statusCode={}", recipientEmail, response.statusCode)
    }
}
