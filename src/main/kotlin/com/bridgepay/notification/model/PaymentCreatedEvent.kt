package com.bridgepay.notification.model

data class PaymentCreatedEvent(
    val id: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val senderId: String,
    val recipientId: String
)
