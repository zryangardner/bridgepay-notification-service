package com.bridgepay.notification.config

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.sendgrid.SendGrid
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NotificationConfig {

    @Value("\${aws.sns.region}")
    private lateinit var snsRegion: String

    @Value("\${sendgrid.api-key}")
    private lateinit var sendGridApiKey: String

    @Bean
    @ConditionalOnProperty(name = ["notifications.enabled"], havingValue = "true")
    fun amazonSns(): AmazonSNS = AmazonSNSClientBuilder.standard()
        .withRegion(snsRegion)
        .build()

    @Bean
    @ConditionalOnProperty(name = ["notifications.enabled"], havingValue = "true")
    fun sendGrid(): SendGrid = SendGrid(sendGridApiKey)
}
