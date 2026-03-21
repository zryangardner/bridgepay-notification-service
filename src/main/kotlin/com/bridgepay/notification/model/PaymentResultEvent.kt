package com.bridgepay.notification.model

data class PaymentResultEvent(
    val paymentId: String,
    val senderId: String,
    val recipientId: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val reason: String? = null
)
