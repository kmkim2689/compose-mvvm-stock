package com.practice.stock_market.util

sealed class Resource<T>(val data: T? = null, val mesage: String? = null) {
    class Success<T>(data: T?): Resource<T>(data)
    class Error<T>(message: String, data: T? = null): Resource<T>(data, message)
    // fow showing progress bar if loading is true
    class Loading<T>(val isLoading: Boolean = true): Resource<T>(null)
}
