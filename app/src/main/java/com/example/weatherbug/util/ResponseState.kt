package com.example.weatherbug.util

sealed class ResponseState<out T> {
    data object Loading : ResponseState<Nothing>()
    data class Success<T>(val data: T) : ResponseState<T>()
    data class Failure(val errorMessage: String) : ResponseState<Nothing>()
}