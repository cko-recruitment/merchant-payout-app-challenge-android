package com.example.androidinterview.domain.model

sealed class PayoutException(message: String) : Exception(message) {
    class InsufficientFunds(message: String) : PayoutException(message)
    class ServiceUnavailable(message: String) : PayoutException(message)
    class Api(message: String) : PayoutException(message)
}
