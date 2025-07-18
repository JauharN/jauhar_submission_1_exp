package com.afin.jauharnafissubmission1expert.core.utils

// Result Class untuk mengecek apakah request berhasil atau tidak
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}