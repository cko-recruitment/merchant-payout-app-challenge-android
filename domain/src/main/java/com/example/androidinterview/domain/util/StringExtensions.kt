package com.example.androidinterview.domain.util

fun String.anonymised(): String {
    if (length <= 8) return "*".repeat(length)
    return take(4) + "*".repeat(length - 8) + takeLast(4)
}
