package com.example.weatherbug.core.util

fun isNoInternetError(message: String?): Boolean {
    if (message.isNullOrBlank()) return false
    val lower = message.lowercase()
    return lower.contains("unable to resolve host") ||
        lower.contains("failed to connect") ||
        lower.contains("network is unreachable") ||
        lower.contains("network unreachable") ||
        lower.contains("connection timed out") ||
        lower.contains("connection refused") ||
        lower.contains("no address associated with hostname") ||
        lower.contains("no internet") ||
        lower.contains("socketexception") ||
        lower.contains("unknownhostexception") ||
        lower.contains("connectexception") ||
        lower.contains("connection reset")
}
